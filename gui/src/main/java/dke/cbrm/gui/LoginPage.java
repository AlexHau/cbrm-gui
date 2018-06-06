package dke.cbrm.gui;

import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.vaadin.annotations.Theme;
import com.vaadin.annotations.Title;
import com.vaadin.server.VaadinRequest;
import com.vaadin.shared.ui.MarginInfo;
import com.vaadin.spring.annotation.SpringUI;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Notification;
import com.vaadin.ui.PasswordField;
import com.vaadin.ui.TextField;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;

import dke.cbrm.business.AuthService;
import dke.cbrm.business.statemachine.CbrmStateService;
import dke.cbrm.business.statemachine.SplitContextEvents;
import dke.cbrm.business.statemachine.StateMachineRunningException;
import dke.cbrm.persistence.model.User;
import lombok.RequiredArgsConstructor;

@SpringUI(path = "/login")
@Title("LoginPage")
@Theme("guitheme")
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class LoginPage extends UI {

    private static final long serialVersionUID = 2954905227586318387L;

    private static final Pattern E_MAIL_PATTERN =
	    Pattern.compile("^[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,6}$",
		    Pattern.CASE_INSENSITIVE);

    private final CbrmStateService cbrmStateService;

    private final DaoAuthenticationProvider daoAuthenticationProvider;

    private final AuthService authService;

    private final PasswordEncoder passwordEncoder;

    private TextField userName, firstName, surname, eMail;

    private PasswordField password;

    private Button loginButton = new Button("Login", this::loginButtonClick),
	    registerButton = new Button("Sign Up", this::registerButtonClick),
	    signUpButton = new Button("Sign Up", this::signUpButtonClick);

    private VerticalLayout fields;

    @Override
    protected void init(VaadinRequest request) {
	setSizeFull();

	userName = new TextField("User:");
	userName.setWidth("300px");

	password = new PasswordField("Password:");
	password.setWidth("300px");
	password.setValue("");

	firstName = new TextField("First Name:");
	firstName.setWidth("300px");
	firstName.setValue("");
	firstName.setVisible(false);

	surname = new TextField("Surname:");
	surname.setWidth("300px");
	surname.setValue("");
	surname.setVisible(false);

	eMail = new TextField("E-Mail address:");
	eMail.setWidth("300px");
	eMail.setValue("");
	eMail.setVisible(false);

	signUpButton.setVisible(false);

	HorizontalLayout hButtonLayout =
		new HorizontalLayout(loginButton, registerButton, signUpButton);

	fields = new VerticalLayout(userName, password, firstName, surname,
		eMail, hButtonLayout);
	fields.setCaption("Please login to access the application");
	fields.setSpacing(true);
	fields.setMargin(new MarginInfo(true, true, true, false));
	fields.setSizeUndefined();

	VerticalLayout uiLayout = new VerticalLayout(fields);
	uiLayout.setSizeFull();
	uiLayout.setComponentAlignment(fields, Alignment.MIDDLE_CENTER);

	setFocusedComponent(userName);

	setContent(uiLayout);
    }

    public void loginButtonClick(Button.ClickEvent e) {
	Authentication auth = new UsernamePasswordAuthenticationToken(
		userName.getValue(), password.getValue());
	Authentication authenticated =
		daoAuthenticationProvider.authenticate(auth);
	SecurityContextHolder.getContext().setAuthentication(authenticated);

	getPage().setLocation("/main");
    }

    public void registerButtonClick(Button.ClickEvent e) {
	switchToSignUpMode(true);
    }

    private void signUpButtonClick(Button.ClickEvent e) {
	StringBuffer strBuffer = new StringBuffer();

	if (StringUtils.isAnyBlank(userName.getValue())) {
	    strBuffer = strBuffer.append(
		    "The User-Name name must not contain blanks or be empty.");
	}

	if (StringUtils.isAnyBlank(firstName.getValue())) {
	    strBuffer = strBuffer.append(
		    "The first name must not contain blanks or be empty.");
	}

	if (StringUtils.isAnyBlank(surname.getValue())) {
	    strBuffer = strBuffer.append(System.lineSeparator())
		    .append("The surname must not contain blanks or be empty.");
	}

	if (StringUtils.isAnyBlank(eMail.getValue())) {
	    strBuffer = strBuffer.append(System.lineSeparator()).append(
		    "The email-address must not contain blanks or be empty.");
	}

	if (!E_MAIL_PATTERN.matcher(eMail.getValue()).find()) {
	    strBuffer = strBuffer.append(System.lineSeparator())
		    .append("The email-address you entered is not valid.");
	}

	if (strBuffer.length() > 0) {
	    Notification.show("Input Errors: ", strBuffer.toString(),
		    Notification.Type.ERROR_MESSAGE);
	} else {
	    User user = new User();
	    user.setEmail(eMail.getValue());
	    user.setFirstName(firstName.getValue());
	    user.setLastName(surname.getValue());
	    user.setPassword(passwordEncoder.encode(password.getValue()));
	    user.setUserName(userName.getValue());

	    authService.registerNewUser(user);

	    switchToSignUpMode(false);
	    Notification.show("Successfully registered as CBRM-User",
		    Notification.Type.HUMANIZED_MESSAGE);
	}
    }

    private void switchToSignUpMode(boolean isSignUp) {
	password.setVisible(!isSignUp);
	loginButton.setVisible(!isSignUp);
	registerButton.setVisible(!isSignUp);

	firstName.setVisible(isSignUp);
	surname.setVisible(isSignUp);
	eMail.setVisible(isSignUp);
	signUpButton.setVisible(isSignUp);

	fields.setCaption(isSignUp ? "Please register for CBRM application"
		: "Please login to access the application");
    }
}

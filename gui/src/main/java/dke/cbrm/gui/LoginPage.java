package dke.cbrm.gui;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import com.vaadin.annotations.Theme;
import com.vaadin.annotations.Title;
import com.vaadin.server.VaadinRequest;
import com.vaadin.shared.ui.MarginInfo;
import com.vaadin.spring.annotation.SpringUI;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.PasswordField;
import com.vaadin.ui.TextField;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;

import lombok.RequiredArgsConstructor;

@SpringUI(path = "/login")
@Title("LoginPage")
@Theme("guitheme")
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class LoginPage extends UI {

    private static final long serialVersionUID = 2954905227586318387L;

    private final DaoAuthenticationProvider daoAuthenticationProvider;

    private TextField user;

    private PasswordField password;

    private Button loginButton = new Button("Login", this::loginButtonClick);

    @Override
    protected void init(VaadinRequest request) {
	setSizeFull();

	user = new TextField("User:");
	user.setWidth("300px");

	password = new PasswordField("Password:");
	password.setWidth("300px");
	password.setValue("");

	VerticalLayout fields = new VerticalLayout(user, password, loginButton);
	fields.setCaption("Please login to access the application");
	fields.setSpacing(true);
	fields.setMargin(new MarginInfo(true, true, true, false));
	fields.setSizeUndefined();

	VerticalLayout uiLayout = new VerticalLayout(fields);
	uiLayout.setSizeFull();
	uiLayout.setComponentAlignment(fields, Alignment.MIDDLE_CENTER);

	setFocusedComponent(user);

	setContent(uiLayout);
    }

    public void loginButtonClick(Button.ClickEvent e) {
	Authentication auth = new UsernamePasswordAuthenticationToken(user.getValue(),
		password.getValue());
	Authentication authenticated = daoAuthenticationProvider.authenticate(auth);
	SecurityContextHolder.getContext().setAuthentication(authenticated);

	getPage().setLocation("/main");
    }
}

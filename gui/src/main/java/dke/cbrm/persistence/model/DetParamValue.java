package dke.cbrm.persistence.model;

import java.time.LocalDateTime;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

import dke.cbrm.persistence.LocalDateTimeConverter;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(exclude = { "parameter" })
public class DetParamValue {

    private @Id @GeneratedValue @Column(
	    name = "ID") Long id;

    @ManyToOne(
	    fetch = FetchType.LAZY,
	    cascade = CascadeType.ALL)
    @JoinColumn(name = "parameter_id")
    private Parameter parameter;

    @Convert(converter = LocalDateTimeConverter.class)
    private LocalDateTime createdAt, modifiedAt;

    private String content;
}

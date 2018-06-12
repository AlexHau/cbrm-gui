package dke.cbrm.persistence;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.Optional;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;

/**
 * This Converter is applied in EntityClasses to convert @{link
 * LocalDateTime}-Objects into @{link java.sql.Timestamp}-Objects or vice versa
 * for writing to and reading from repository respectively (Source:
 * https://dzone.com/articles/dealing-with-javas-localdatetime-in-jpa
 * [27.11.2017]) - Without this Converter @{link LocalDateTime}-Members of
 * Entity-Classes would be persisted as 'VARBINARY'-Type in repository
 */
@Converter(autoApply = true)
public class LocalDateTimeConverter implements AttributeConverter<LocalDateTime, Timestamp> {

	@Override

	public Timestamp convertToDatabaseColumn(LocalDateTime localDateTime) {
		return Optional.ofNullable(localDateTime).map(Timestamp::valueOf).orElse(null);
	}

	@Override

	public LocalDateTime convertToEntityAttribute(Timestamp timestamp) {
		return Optional.ofNullable(timestamp).map(Timestamp::toLocalDateTime).orElse(null);

	}

}
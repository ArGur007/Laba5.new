package ru.laba5.storage;

import com.opencsv.bean.AbstractBeanField;
import com.opencsv.exceptions.CsvDataTypeMismatchException;
import java.time.Instant;

public class InstantConverter extends AbstractBeanField<Instant, String> {

    @Override
    protected Instant convert(String value) throws CsvDataTypeMismatchException {
        if (value == null || value.isEmpty()) {
            return null;
        }
        try {
            return Instant.parse(value);
        } catch (Exception e) {
            CsvDataTypeMismatchException ex = new CsvDataTypeMismatchException(
                    "Не удалось распарсить Instant: " + value);
            ex.initCause(e);   // сохраняем причину
            throw ex;
        }
    }

    @Override
    protected String convertToWrite(Object value) throws CsvDataTypeMismatchException {
        if (value == null) {
            return "";
        }
        return ((Instant) value).toString();
    }
}
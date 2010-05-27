package transactionality;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.annotation.ElementType;

/** @author Christoffer Lerno */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface Optional
{

}

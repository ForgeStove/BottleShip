package io.github.forgestove.bottle_ship.config.annotation;
import java.lang.annotation.*;
/**
 * Marks an integer field as a color value.
 * This will display a color picker in the config GUI.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface ColorValue {
	/**
	 * Whether the color includes an alpha channel.
	 *
	 * @return true if the color has alpha (ARGB), false for RGB only
	 */
	boolean hasAlpha() default false;
}


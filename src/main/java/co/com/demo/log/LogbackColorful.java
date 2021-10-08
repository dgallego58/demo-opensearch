package co.com.demo.log;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.pattern.color.ANSIConstants;
import ch.qos.logback.core.pattern.color.ForegroundCompositeConverterBase;

public class LogbackColorful extends ForegroundCompositeConverterBase<ILoggingEvent> {

    @Override
    protected String getForegroundColorCode(ILoggingEvent event) {
        Level level = event.getLevel();
        switch (level.toInt()) {
            // El nivel de ERROR es rojo
            case Level.ERROR_INT:
                return ANSIConstants.RED_FG;
            // el nivel WARN es amarillo
            case Level.WARN_INT:
                return ANSIConstants.YELLOW_FG;
            // El nivel de INFO es azul
            case Level.INFO_INT:
                return ANSIConstants.BLUE_FG;
            // El nivel de DEPURACIÓN es verde
            case Level.DEBUG_INT:
                return ANSIConstants.GREEN_FG;
            // Otro es el color predeterminado
            default:
                return ANSIConstants.DEFAULT_FG;
        }
    }
}

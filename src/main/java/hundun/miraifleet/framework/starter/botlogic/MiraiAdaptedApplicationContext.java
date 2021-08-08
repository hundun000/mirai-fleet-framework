package hundun.miraifleet.framework.starter.botlogic;

import org.springframework.context.annotation.AnnotationConfigApplicationContext;

/**
 * @author hundun
 * Created on 2021/07/01
 */
public class MiraiAdaptedApplicationContext extends AnnotationConfigApplicationContext {
    
    public MiraiAdaptedApplicationContext(boolean lateRefresh) {
        super();
        this.setClassLoader(this.getClass().getClassLoader());
        this.scan("hundun.miraifleet.frame.starter",
                "hundun.miraifleet.frame.core"
                );
        if (!lateRefresh) {
            this.refresh();
        }
    }
}

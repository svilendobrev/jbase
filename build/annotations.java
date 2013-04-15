/*-
//declare
import java.lang.annotation;
@annotation.Retention( annotation.RetentionPolicy.RUNTIME)
@annotation.Target( annotation.ElementType.FIELD) //list of .. FIELD METHOD TYPE
public @interface Funky {
    //nothing
    //or single arg named value:  Type value()  default somevalue; used without key
    //or multiple args named anyhow, used as key-value list
}

//scan@runtime:
for (Field someField : someAnnotatedClass.getClass().getDeclaredFields()) {
    if (someField.isAnnotationPresent(Funky.class))
       System.out.println("This field is funky: " + someField.getName());

}

//apply
import org.foo.annotations.Funky;
public class someAnnotatedClass {
    @Funky
    String   funky;
    String   non_funky;
    @ExtraFunky( a=1, b=12)
    String   super_funky;
}

*/


package application.resolver.method;

import org.juzu.Render;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public class A
{

   @Render
   public void noArg() { }

   @Render
   public void fooArg(String foo) { }

}
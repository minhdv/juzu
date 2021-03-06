package org.juzu.impl.plugin.asset;

import org.juzu.Response;
import org.juzu.impl.application.ApplicationException;
import org.juzu.impl.application.metadata.ApplicationDescriptor;
import org.juzu.impl.request.RequestLifeCycle;
import org.juzu.impl.request.Request;
import org.juzu.impl.utils.Tools;
import org.juzu.plugin.asset.Assets;
import org.juzu.plugin.asset.Script;
import org.juzu.plugin.asset.Stylesheet;
import org.juzu.request.Phase;
import org.juzu.io.CharStream;

import javax.inject.Inject;
import java.io.IOException;
import java.util.Iterator;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public class AssetLifeCycle extends RequestLifeCycle
{

   /** . */
   private static final String[] EMPTY_STRING_ARRAY = new String[0];

   /** . */
   private final String[] scripts;

   /** . */
   private final String[] stylesheets;

   @Inject
   public AssetLifeCycle(ApplicationDescriptor desc)
   {
      String[] scripts;
      String[] stylesheets;
      Class<?> packageClass = desc.getPackageClass();
      Assets assets = packageClass.getAnnotation(Assets.class);
      if (assets != null)
      {
         Script[] scriptDecls = assets.scripts();
         if (scriptDecls.length > 0)
         {
            scripts = new String[scriptDecls.length];
            for (int i = 0;i < scriptDecls.length;i++)
            {
               scripts[i] = scriptDecls[i].src();
            }
         }
         else
         {
            scripts = EMPTY_STRING_ARRAY;
         }
         Stylesheet[] stylesheetDecls = assets.stylesheets();
         if (stylesheetDecls.length > 0)
         {
            stylesheets = new String[stylesheetDecls.length];
            for (int i = 0;i < stylesheetDecls.length;i++)
            {
               stylesheets[i] = stylesheetDecls[i].src();
            }
         }
         else
         {
            stylesheets = EMPTY_STRING_ARRAY;
         }
      }
      else
      {
         scripts = EMPTY_STRING_ARRAY;
         stylesheets = EMPTY_STRING_ARRAY;
      }

      //
      this.stylesheets = stylesheets;
      this.scripts = scripts;
   }

   @Override
   public void invoke(Request request) throws ApplicationException
   {
      request.invoke();

      //
      if (request.getContext().getPhase() == Phase.RENDER)
      {
         Response response = request.getResponse();
         if (response instanceof Response.Render && (scripts.length > 0 || stylesheets.length > 0))
         {
            final Response.Render render = (Response.Render)response;

            //
            Response.Render assetRender = new Response.Render()
            {

               @Override
               public Iterator<String> getScripts()
               {
                  return Tools.append(render.getScripts(), scripts);
               }

               @Override
               public Iterator<String> getStylesheets()
               {
                  return Tools.append(render.getStylesheets(), stylesheets);
               }

               @Override
               public String getTitle()
               {
                  return render.getTitle();
               }

               @Override
               public void send(CharStream stream) throws IOException
               {
                  render.send(stream);
               }
            };

            // Use our response
            request.setResponse(assetRender);
         }
      }
   }
}

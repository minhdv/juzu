/*
 * Copyright (C) 2011 eXo Platform SAS.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */

package http.lifecycle;

import org.juzu.Action;
import org.juzu.Resource;
import org.juzu.Response;
import org.juzu.View;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public class A
{
   
   @Action
   public Response.Update action()
   {
      return A_.done("d");
   }
   
   @View
   public Response.Content index()
   {
      return Response.render(A_.actionURL().toString());
   }

   @View
   public Response.Content done(String p)
   {
      if ("d".equals(p))
      {
         return Response.render(A_.resourceURL().toString());
      }
      else
      {
         return Response.render("<html><body>fail</body></html>");
      }
   }

   @Resource
   public Response.Content.Resource resource()
   {
      return Response.status(200, "<html><body>done</body></html>");
   }
}

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

package org.juzu.impl.tags;

import org.juzu.impl.compiler.CompilationException;
import org.juzu.impl.metamodel.MetaModelError;
import org.juzu.impl.template.ast.ASTNode;
import org.juzu.impl.template.compiler.ExtendedTagHandler;
import org.juzu.impl.spi.template.TemplateStub;
import org.juzu.impl.template.compiler.ProcessPhase;
import org.juzu.impl.template.compiler.Template;
import org.juzu.template.Renderable;
import org.juzu.template.TemplateRenderContext;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Map;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public class DecorateTag extends ExtendedTagHandler
{

   /** . */
   static final ThreadLocal<Renderable> current = new ThreadLocal<Renderable>();

   @Override
   public void process(ProcessPhase phase, ASTNode.Tag tag, Template t)
   {
      ASTNode current =  tag;
      while (true)
      {
         if (current instanceof ASTNode.Block)
         {
            current = ((ASTNode.Block)current).getParent();
         }
         else
         {
            break;
         }
      }

      //
      ASTNode.Template template = (ASTNode.Template)current;
      for (ASTNode.Block child : new ArrayList<ASTNode.Block<?>>(template.getChildren()))
      {
         if (child != tag)
         {
            tag.addChild(child);
         }
      }

      //
      if (tag.getParent() != template)
      {
         template.addChild(tag);
      }
   }

   @Override
   public void compile(ProcessPhase phase, ASTNode.Tag tag, Template t)
   {
      String path = tag.getArgs().get("path");
      Template resolved = phase.resolveTemplate(path);
      if (resolved == null)
      {
         throw new CompilationException(MetaModelError.TEMPLATE_NOT_RESOLVED, path);
      }
   }

   @Override
   public void render(TemplateRenderContext context, Renderable body, Map<String, String> args) throws IOException
   {
      current.set(body);
      try
      {
         String path = args.get("path");
         TemplateStub template = context.resolveTemplate(path);
         template.render(context);
      }
      finally
      {
         current.set(null);
      }
   }
}

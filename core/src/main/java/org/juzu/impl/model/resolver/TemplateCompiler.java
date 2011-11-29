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

package org.juzu.impl.model.resolver;

import org.juzu.impl.compiler.CompilationException;
import org.juzu.impl.model.ErrorCode;
import org.juzu.impl.model.meta.TemplateMetaModel;
import org.juzu.impl.model.processor.ProcessingContext;
import org.juzu.impl.template.ASTNode;
import org.juzu.impl.template.ParseException;
import org.juzu.impl.template.TemplateCompilationContext;
import org.juzu.impl.utils.Content;
import org.juzu.impl.utils.FQN;
import org.juzu.impl.utils.MethodInvocation;
import org.juzu.impl.utils.Spliterator;

import javax.lang.model.element.Element;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
class TemplateCompiler
{

   /** . */
   private TemplateMetaModel templateMetaModel;

   /** . */
   private final Map<String, Template> templates;

   /** . */
   private ArrayList<Template> added;

   /** . */
   private final ProcessingContext env;

   TemplateCompiler(
      TemplateMetaModel templateMetaModel,
      Map<String, Template> templates,
      ProcessingContext env)
   {
      this.templateMetaModel = templateMetaModel;
      this.templates = templates;
      this.env = env;

   }

   List<Template> resolve()
   {
      added = new ArrayList<Template>();
      context.resolveTemplate(templateMetaModel.getPath());
      return added;
   }

   private final TemplateCompilationContext context = new TemplateCompilationContext()
   {
      @Override
      public void resolveTemplate(String path)
      {
         Template template = templates.get(path);

         //
         if (template == null)
         {
            // Validate and analyse the path
            Matcher matcher = ModelResolver.TEMPLATE_PATH_PATTERN.matcher(path);
            if (!matcher.matches())
            {
               Element elt = env.get(templateMetaModel.getRefs().iterator().next().getHandle());
               throw new CompilationException(elt, ErrorCode.ILLEGAL_PATH, path);
            }
            String folder = matcher.group(1);
            String rawName = matcher.group(2);
            String extension = matcher.group(3);

            // Resolve the template fqn and the template name
            String fqn = templateMetaModel.getApplication().getTemplatesQN().getValue();
            for (String name: Spliterator.split(folder + rawName, '/'))
            {
               if (fqn.length() == 0)
               {
                  fqn = name;
               }
               else
               {
                  fqn += "." +  name;
               }
            }
            FQN stubFQN = new FQN(fqn);

            // Get source
            Content content = env.resolveResource(stubFQN, extension);
            if (content == null)
            {
               throw new CompilationException(env.get(templateMetaModel.getRefs().iterator().next().getHandle()), ErrorCode.TEMPLATE_NOT_FOUND, fqn);
            }

            // Parse to AST
            ASTNode.Template templateAST;
            try
            {
               templateAST = ASTNode.Template.parse(content.getCharSequence());
            }
            catch (ParseException e)
            {
               Element elt = env.get(templateMetaModel.getRefs().iterator().next().getHandle());
               throw new CompilationException(elt, ErrorCode.TEMPLATE_SYNTAX_ERROR, fqn);
            }

            // Obtain template parameters
            ArrayList<ASTNode.Tag> paramTags = new ArrayList<ASTNode.Tag>();
            collectParams(templateAST, paramTags);
            LinkedHashSet<String> parameters = null;
            if (paramTags.size() > 0)
            {
               parameters = new LinkedHashSet<String>();
               for (ASTNode.Tag paramTag : paramTags)
               {
                  String paramName = paramTag.getArgs().get("name");
                  parameters.add(paramName);
               }
            }

            // Add template to application
            templates.put(path, template = new Template(
               templateMetaModel.getPath(),
               templateAST,
               stubFQN,
               extension,
               path,
               parameters,
               content.getLastModified()));

            // Process template
            templateAST.process(this);

            //
            added.add(template);
         }
      }

      @Override
      public MethodInvocation resolveMethodInvocation(String typeName, String methodName, Map<String, String> parameterMap)
      {
         throw new UnsupportedOperationException();
      }
   };


   private void collectParams(ASTNode<?> node, List<ASTNode.Tag> tags)
   {
      for (ASTNode.Block child : node.getChildren())
      {
         collectParams(child, tags);
      }
      if (node instanceof ASTNode.Tag)
      {
         ASTNode.Tag tag = (ASTNode.Tag)node;
         if (tag.getName().equals("param"))
         {
            tags.add(tag);
         }
      }
   }
}
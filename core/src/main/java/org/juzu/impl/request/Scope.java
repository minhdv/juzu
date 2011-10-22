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

package org.juzu.impl.request;

import org.juzu.FlashScoped;
import org.juzu.IdentityScoped;
import org.juzu.RequestScoped;
import org.juzu.SessionScoped;

import java.lang.annotation.Annotation;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public enum Scope
{

   REQUEST(RequestScoped.class)
   {
      @Override
      public boolean isActive(Request context)
      {
         return true;
      }
   },

   SESSION(SessionScoped.class)
   {
      @Override
      public boolean isActive(Request context)
      {
         return true;
      }
   },

   /**
    * todo : study more in depth how flash scoped is propagated to other phase, specially the resource phase
    * todo : that should kind of have an ID.
    */
   FLASH(FlashScoped.class)
   {
      @Override
      public boolean isActive(Request context)
      {
         return true;
      }
   },

   IDENTITY(IdentityScoped.class)
   {
      @Override
      public boolean isActive(Request request)
      {
         return false;
      }
   };
   
   /** . */
   private final Class<? extends Annotation> annotationType;

   Scope(Class<? extends Annotation> annotationType)
   {
      this.annotationType = annotationType;
   }

   public abstract boolean isActive(Request context);

   public Class<? extends Annotation> getAnnotationType()
   {
      return annotationType;
   }
}

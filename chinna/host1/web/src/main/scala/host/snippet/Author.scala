/*
 * Copyright 2008 WorldWide Conferencing, LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions
 * and limitations under the License.
 */
package host {
package snippet {

import _root_.scala.xml.{NodeSeq,Text}

import _root_.net.liftweb._
import http._
import S._
import common._
import util._
import Helpers._

import _root_.javax.persistence.{EntityExistsException,PersistenceException}

import host.model._
import Model._

class AuthorOps extends Loggable {
  def list (xhtml : NodeSeq) : NodeSeq = {
    val authors = Model.createNamedQuery[Author]("findAllAuthors").getResultList()

    authors.flatMap(author =>
      bind("author", xhtml,
	   "name" -> Text(author.name),
	   "count" -> SHtml.link("/books/search.html", {() =>
	     BookOps.resultVar(Model.createNamedQuery[Book]("findBooksByAuthor", "id" ->author.id).getResultList().toList)
	     }, Text(author.books.size().toString)),
	   "edit" -> SHtml.link("add.html", () => authorVar(author), Text(?("Edit")))))
  }

  // Set up a requestVar to track the author object for edits and adds
  object authorVar extends RequestVar(new Author())
  def author = authorVar.is

  def add (xhtml : NodeSeq) : NodeSeq = {
    def doAdd () = {
      if (author.name.length == 0) {
	error("emptyAuthor", "The author's name cannot be blank")
      } else {
	try {
	  Model.mergeAndFlush(author)
	  redirectTo("list.html")
	} catch {
	  case ee : EntityExistsException => error("Author already exists")
	  case pe : PersistenceException => error("Error adding author"); logger.error("Error adding author", pe)
	}
      }
    }

    // Hold a val here so that the "id" closure holds it when we re-enter this method
    val currentId = author.id

    bind("author", xhtml,
	 "id" -> SHtml.hidden(() => author.id = currentId),
	 "name" -> SHtml.text(author.name, author.name = _),
	 "submit" -> SHtml.submit(?("Save"), doAdd))
  }
}
}
}

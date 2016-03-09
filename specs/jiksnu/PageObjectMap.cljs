(ns jiksnu.PageObjectMap
  (:require [jiksnu.pages.LoginPage :refer [LoginPage]]))

(def pages
  #js
  {"login" LoginPage})

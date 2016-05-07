(ns jiksnu.PageObjectMap
  (:require [jiksnu.pages.LoginPage :refer [LoginPage]]
            [jiksnu.pages.HomePage :refer [HomePage]]
            [jiksnu.pages.RegisterPage :refer [RegisterPage]]
            [jiksnu.pages.UserIndexPage :refer [UserIndexPage]]))

(def pages
  #js
  {"login" LoginPage
   "home"  HomePage
   "register" RegisterPage
   "user index" UserIndexPage})

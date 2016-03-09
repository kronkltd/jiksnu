(ns jiksnu.PageObjectMap
  (:require [jiksnu.pages.LoginPage :refer [LoginPage]]
            [jiksnu.pages.HomePage :refer [HomePage]]
            [jiksnu.pages.RegisterPage :refer [RegisterPage]]))

(def pages
  #js
  {"login" LoginPage
   "home"  HomePage
   "register" RegisterPage
   })

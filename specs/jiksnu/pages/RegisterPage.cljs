(ns jiksnu.pages.RegisterPage)

(defn RegisterPage
  [])

(set! (.-get RegisterPage) (fn [] (.get js/browser "/main/login")))

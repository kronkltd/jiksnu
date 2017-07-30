(ns jiksnu.pages.UserIndexPage)

(defn UserIndexPage
  [])

(set! (.-get (.-prototype UserIndexPage))
      (fn [] (.get js/browser "/main/users")))

(set! (.-waitForLoaded (.-prototype UserIndexPage))
      (fn [] (.wait js/browser (constantly true))))

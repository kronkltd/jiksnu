(ns jiksnu.pages.HomePage)

(defn HomePage
  [])

(set! (.-get (.-prototype HomePage))
      (fn [] (.get js/browser "/")))

(set! (.-waitForLoaded (.-prototype HomePage))
      (fn []
        (this-as
         this
         (.wait
          js/browser
          (fn []
            (js/console.log "Waiting for loaded")
            true)))))

(ns jiksnu.modules.xmpp.sections.user-sections
  (:use [ciste.sections :only [defsection]]
        [ciste.sections.default :only [show-section]])
  (:require [clojure.tools.logging :as log]
            [jiksnu.namespace :as ns]
            [lamina.trace :as trace])
  (:import jiksnu.model.User))

;; TODO: This should be the vcard format
(defsection show-section [User :xmpp]
  [^User user & options]
  (let [{:keys [name avatar-url]} user]
    (h/html
     ["vcard"
      {"xmlns" ns/vcard}
      ["fn" ["text" (:name user)]]
      [:nickname (:username user)]
      [:url (:url user)]
      [:n
       [:given (:first-name user)]
       [:family (:last-name user)]
       [:middle (:middle-name user)]
       ]
      (when avatar-url
        ["photo"
         ["uri" avatar-url]])])))


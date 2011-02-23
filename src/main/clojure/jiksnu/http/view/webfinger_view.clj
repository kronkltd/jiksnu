(ns jiksnu.http.view.webfinger-view
  (:use ciste.core
        ciste.view
        jiksnu.config
        jiksnu.http.controller.webfinger-controller))

(defview #'host-meta :html
  [request _]
  (let [domain (:domain (config))]
    {:body
     ["XRD" {"xmlns" xrd-ns
             "xmlns:hm" host-meta-ns}
      ["hm:Host" domain]
      ["Link" {"rel" "lrdd"
               "template" (str "http://"
                               domain
                               "/webfinger?q={uri}")}
       ["Title" {} "Resource Descriptor"]]]}))

(defview #'user-meta :html
  [request user]
  {:body
   ["XRD" {"xmlns" xrd-ns

           }
    ["Subject" {} (str "acct:"
                       (:username user)
                       "@" (:domain user)
                       )]
    ["Alias" {} (uri user)]
    ["Link" {"rel" "describedby"
             "href" (uri user)} ]

    ]
   }
  )

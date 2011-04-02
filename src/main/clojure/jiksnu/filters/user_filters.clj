(ns jiksnu.filters.user-filters)

(defn rule-element?
  [^Element element]
  (= (.getName element) "acl-rule"))

(defn rule-map
  [rule]
  (let [^Element action-element (.getChild rule "acl-action")
        ^Element subject-element (.getChild rule "acl-subject")]
    {:subject (.getAttribute subject-element "type")
     :permission (.getAttribute action-element "permission")
     :action (.getCData action-element)}))

(defn property-map
  [user property]
  (let [child-elements (children property)
        rule-elements (filter rule-element? child-elements)
        type-element (first (filter (comp not rule-element?) child-elements))]
    {:key (.getName property)
     :type (.getName type-element)
     :value (.getCData type-element)
     :rules (map rule-map rule-elements)
     :user user}))

(defn process-vcard-element
  [element]
  (fn [vcard-element]
    (map (partial property-map (current-user))
         (children vcard-element))))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; create
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(deffilter #'create :http
  [action request]
  (let [{:keys [params]} request]
    (action params)))

;; TODO: this one wasn't working in the first place
(deffilter #'create :xmpp
  [action request]
  (let [{:keys [items]} request]
    (let [properties
          (flatten
           (map process-vcard-element items))]
      (action properties))))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; delete
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(deffilter #'delete :http
  [action request]
  (let [{{id "id"} :params} request]
    (action id)))

(deffilter #'delete :xmpp
  [action request]
  ;; TODO: implement
  )

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; edit
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(deffilter #'edit :http
  [action request]
  (let [user (show request)]
    user))


;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; fetch-remote
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(deffilter #'fetch-remote :xmpp
  [action request]
  (model.user/fetch-by-jid (:to request)))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; index
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(deffilter #'index :http
  [action request]
  (let [{params :params} request]
    (action params)))

(deffilter #'index :xmpp
  [action request]
  '()
  )

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; profile
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(deffilter #'profile :http
  [action request]
  (if-let [user (current-user)]
    user (error "no user")))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; register
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(deffilter #'register :http
  [action request]
  true)

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; remote-create
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(deffilter #'remote-create :xmpp
  [action request]
  (let [{:keys [to from payload]} request
        user (model.user/fetch-by-jid from)
        vcard (first (children payload))
        gender (.getCData (find-children vcard "/vcard/gender"))
        name (.getCData (find-children vcard "/vcard/fn/text"))
        first-name (.getCData (find-children vcard "/vcard/n/given/text"))
        last-name (.getCData (find-children vcard "/vcard/n/surname/text"))
        url (.getCData (find-children vcard "/vcard/url/uri"))
        avatar-url (.getCData (find-children vcard "/vcard/photo/uri"))
        new-user {:gender gender
                  :name name
                  :first-name first-name
                  :last-name last-name
                  :url url
                  :avatar-url avatar-url}
        merged-user (merge user new-user)]
    (jiksnu.model.user/update merged-user)))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; remote-profile
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(deffilter #'remote-profile :http
  [action request]
  (let [{{id "id"} :params} request]
    (let [user (model.user/fetch-by-id id)]
      user)))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; show
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(deffilter #'show :http
  [action request]
  (let [{{id "id"} :params} request]
    (action id)))

;; TODO: This action is working off of a jid
(deffilter #'show :xmpp
  [action request]
  (let [{:keys [to]} request]
    (action to)))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; update
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(deffilter #'update :http
  [action {{username "username" :as params} :params :as request}]
  (let [user (model.user/show username (:domain (config)))]
    (let [new-params
          (-> (into {}
                    (map
                     (fn [[k v]]
                       (if (not= v "")
                         [(keyword k) v]))
                     params))
              (dissoc :id)
              #_(assoc :_id id))]
      (model.user/update new-params))))

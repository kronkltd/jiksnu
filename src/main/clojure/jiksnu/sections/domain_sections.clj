(ns jiksnu.sessions.domain-sections)

(defsection uri [Domain :html]
  [domain & options]
  (str "/domains/" (:_id domain)))

(defsection add-form [Domain :html]
  [domain & options]
  [:div
   [:p "Add Domain"]
   (f/form-to
    [:post "/domains"]
    [:p
     (f/label :domain "Domain")
     (f/text-field :domain (:_id domain))
     (f/submit-button "Add")])])

(defsection index-line [Domain :html]
  [domain & options]
  [:tr
   [:td
    [:a {:href (uri domain)} (:_id domain)]]
   [:td (:osw domain)]
   [:td
    [:a {:href (str "http://" (:_id domain)
                    "/.well-known/host-meta")} "Host-Meta"]]
   [:td (f/form-to
         [:post (str "/domains/" (:_id domain) "/discover")]
         (f/submit-button "Discover"))]
   [:td (f/form-to
         [:delete (uri domain)]
         (f/submit-button "Delete")
         )]
   ])

(defsection index-section [Domain :html]
  [domains & options]
  [:table
   [:tr
    [:th "Name"]
    [:th "OSW Enabled?"]
    [:th "Host-Meta"]
    [:th "Discover"]
    [:th "Delete"]
    ]
   (map index-line  domains)])

(defsection index-block [Domain :html]
  [domains & options]
  [:div
    [:p "index domains"]
   (index-section domains)
   (add-form (Domain.))])


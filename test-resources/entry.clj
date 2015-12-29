{:extensions
 [{:children ["http://activitystrea.ms/schema/1.0/post"],
   :attributes {:xmlns "http://activitystrea.ms/spec/1.0/"},
   :name "verb"}
  {:children
   [{:children ["http://onesocialweb.org/spec/1.0/object/status"],
     :attributes {},
     :name "object-type"}
    {:children ["to be or not to be ?"],
     :attributes {:type "text/plain"},
     :name "content"}],
   :attributes {:xmlns "http://activitystrea.ms/spec/1.0/"},
   :name "object"}
  {:children
   [{:children ["http://onesocialweb.org/spec/1.0/acl/action/view"],
     :attributes
     {:permission "http://onesocialweb.org/spec/1.0/acl/permission/grant"},
     :name "acl-action"}
    {:children [],
     :attributes {:type "http://onesocialweb.org/spec/1.0/acl/subject/everyone"},
     :name "acl-subject"}],
   :attributes {:xmlns "http://onesocialweb.org/spec/1.0/"},
   :name "acl-rule"}],
 :authors [{:name "daniel@renfer.name/dell"}],
 :published "2010-10-03T23:37:48.948Z",
 :title "to be or not to be ?",
 :id "urn:uuid:567423F9186BF976271286149068956"}

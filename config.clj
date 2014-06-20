{:default
 {:registration-enabled true,
  :print
  {:actions false,
   :matchers false,
   :packet false,
   :predicates false,
   :request false,
   :routes false,
   :triggers false},
  :use-pipeline true,
  :database {:host "localhost"},
  :site
  {:default {:timezone "+0:00", :language "en-us"},
   :name "Jiksnu",
   :brought-by {:name "Jiksnu", :url "http://jiksnu.org/"},
   :closed false,
   :private false,
   :email "admin@jiksnu.com",
   :limit {:text 140, :dupe 60},
   :theme "classic",
   :available-themes
   ["amelia"
    "cerulean"
    "classic"
    "cyborg"
    "journal"
    "readable"
    "simplex"
    "slate"
    "spacelab"
    "spruce"
    "superhero"
    "united"],
   :invite-only false},
  :swank {:port "4005"},
  :services
  ["ciste.service.aleph"
   "ciste.services.nrepl"
   "jiksnu.plugins.google-analytics"],
  :xmpp
  {:c2s [5222],
   :s2s [5269],
   :auth-db "jiksnu.xmpp.user_repository",
   :user-db "jiksnu.xmpp.user_repository",
   :plugins ["jiksnu" "message"],
   :components []},
  :http {:port 8080, :websocket true, :handler "jiksnu.routes/app"},
  :run-triggers true,
  :ciste.services.nrepl/port 7888,
  :salmon {:verify true},
  :triggers {:thread-count 1},
  :modules [],
  :debug false,
  :admins ["admin"],
 :development
 {:registration-enabled false,
  :print
  {:actions true,
   :trigers true,
   :packet true,
   :routes false,
   :request false,
   :triggers false},
  :database {:name "renfer_name"},
  :site {:theme "classic", :name "Renfer.name", :email nil},
  :domain "renfer.name",
  :xmpp {:c2s [5222 5235], :s2s [5269 5275]},
  :http {:port 8082},
  :jiksnu.plugins.google-analytics/account-id "UA-93750-5",
  :admins ["daniel"],
  :htmlOnly false},
 :test
 {:database {:name "jiksnu_test"},
  :domain "localhost",
  :ciste.services.nrepl/port 7889,
  :http {:port 8175},
  :htmlOnly true,
  :services [],
  :print {:request true},
  :run-triggers false},
 :integration
 {:database {:name "jiksnu_integration"},
  :domain "localhost",
  :ciste.services.nrepl/port 7890,
  :http {:port 8175},
  :htmlOnly true,
  :print
  {:actions true, :request false, :routes false, :triggers false},
  :services ["ciste.service.aleph"],
  :run-triggers true}}

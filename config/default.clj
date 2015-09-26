{:site
 {:closed false,
  :email "admin@jiksnu.com",
  :invite-only false,
  :limit {:text 140, :dupe 60},
  :private false,
  :default {:timezone "+0:00", :language "en-us"},
  :name "Jiksnu",
  :brought-by {:name "Jiksnu", :url "https://jiksnu.org/"}},
 :admins ["admin"],
 :run-triggers true,
 :debug false,
 :salmon {:verify true},
 :triggers {:thread-count 1},
 :print
 {:actions true,
  :matchers false,
  :packet false,
  :predicates false,
  :request false,
  :routes true,
  :triggers false},
 :use-pipeline true,
 :registration-enabled true,
 :ciste.services.nrepl/port 7888,
 :xmpp
 {:c2s [5222],
  :s2s [5269],
  :auth-db "jiksnu.xmpp.user_repository",
  :user-db "jiksnu.xmpp.user_repository",
  :plugins ["jiksnu" "message"],
  :components []},
 :airbrake
 {:enabled true,
  :host "errbit.kronkltd.net",
  :key "28825eeef90708abf4702ec9d91eadc6"},
 :http
 {:port 8080,
  :websocket true,
  :handler "jiksnu.modules.web.routes/app"},
 :database {:host "localhost"},
 :swank {:port "4005"},
 :services
 ["ciste.services.http-kit"
  "ciste.services.nrepl"
  "jiksnu.plugins.google-analytics"]
 }

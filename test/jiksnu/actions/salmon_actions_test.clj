(ns jiksnu.actions.salmon-actions-test
  (:use (ciste [config :only [with-environment]]
               [debug :only [spy]])
        (clj-factory [core :only [factory]])
        (clojure [test :only [deftest]])
        midje.sweet
        (jiksnu [test-helper :only [test-environment-fixture]])
        jiksnu.actions.salmon-actions)
  (:require [clojure.java.io :as io]
            (jiksnu [model :as model])
            (jiksnu.actions [activity-actions :as actions.activity]
                            [user-actions :as actions.user])
            (jiksnu.helpers [activity-helpers :as helpers.activity])
            (jiksnu.model [signature :as model.signature]))
  (:import java.security.Key
           jiksnu.model.User))



(test-environment-fixture

 (def armored-n "1PAkgCMvhHGg-rqBDdaEilXCi0b2EyO-JwSkZqjgFK5HrS0vy4Sy8l3CYbcLxo6d3QG_1SbxtlFoUo4HsbMTrDtV7yNlIJlcsbWFWkT3H4BZ1ioNqPQOKeLIT5ZZXfSWCiIs5PM1H7pSOlaItn6nw92W53205YXyHKHmZWqDpO0=")
 (def armored-e "AQAB")

 (def val-env {:sig
               "iogDPcy9VnGmdG950EV_HniHxp7IenG6Dcja3azVl7IpJqmFsKg4JUIyQiRCrNHnUCK6n9kXtpz2vKyC6w7E6UqIqYmHkLgf19I2ixqOtweO0r83M4Uh7gI5qVuvp8PLo6LtMBgR7GJ
Mu1BJ4WNOIByHAlVEb5SzMAsnxtzIzIQ=",
               :data
               "PD94bWwgdmVyc2lvbj0iMS4wIiBlbmNvZGluZz0iVVRGLTgiID8-PGVudHJ5IHhtbG5zPSJodHRwOi8vd3d3LnczLm9yZy8yMDA1L0F0b20iIHhtbG5zOnRocj0iaHR0cDovL3B1cmw
ub3JnL3N5bmRpY2F0aW9uL3RocmVhZC8xLjAiIHhtbG5zOmFjdGl2aXR5PSJodHRwOi8vYWN0aXZpdHlzdHJlYS5tcy9zcGVjLzEuMC8iIHhtbG5zOmdlb3Jzcz0iaHR0cDovL3d3dy5n
ZW9yc3Mub3JnL2dlb3JzcyIgeG1sbnM6b3N0YXR1cz0iaHR0cDovL29zdGF0dXMub3JnL3NjaGVtYS8xLjAiIHhtbG5zOnBvY289Imh0dHA6Ly9wb3J0YWJsZWNvbnRhY3RzLm5ldC9zc
GVjLzEuMCIgeG1sbnM6bWVkaWE9Imh0dHA6Ly9wdXJsLm9yZy9zeW5kaWNhdGlvbi9hdG9tbWVkaWEiIHhtbG5zOnN0YXR1c25ldD0iaHR0cDovL3N0YXR1cy5uZXQvc2NoZW1hL2FwaS
8xLyI-CiA8aWQ-dGFnOmtyb25rbHRkLm5ldCwyMDExLTExLTEzOnVuZm9sbG93OjE6MjoxOTcwLTAxLTAxVDAwOjAwOjAwKzAwOjAwPC9pZD4KIDx0aXRsZT5VbmZvbGxvdzwvdGl0bGU
-CiA8Y29udGVudCB0eXBlPSJodG1sIj5EYW5pZWwgUmVuZmVyIHN0b3BwZWQgZm9sbG93aW5nIGR1Y2suPC9jb250ZW50PgogPGFjdGl2aXR5OnZlcmI-aHR0cDovL29zdGF0dXMub3Jn
L3NjaGVtYS8xLjAvdW5mb2xsb3c8L2FjdGl2aXR5OnZlcmI-CiA8cHVibGlzaGVkPjIwMTEtMTEtMTNUMTk6MzY6MjYrMDA6MDA8L3B1Ymxpc2hlZD4KIDx1cGRhdGVkPjIwMTEtMTEtM
TNUMTk6MzY6MjYrMDA6MDA8L3VwZGF0ZWQ-CiA8YXV0aG9yPgogIDxhY3Rpdml0eTpvYmplY3QtdHlwZT5odHRwOi8vYWN0aXZpdHlzdHJlYS5tcy9zY2hlbWEvMS4wL3BlcnNvbjwvYW
N0aXZpdHk6b2JqZWN0LXR5cGU-CiAgPHVyaT5odHRwOi8va3JvbmtsdGQubmV0L3VzZXIvMTwvdXJpPgogIDxuYW1lPmR1Y2s8L25hbWU-CiAgPGxpbmsgcmVsPSJhbHRlcm5hdGUiIHR
5cGU9InRleHQvaHRtbCIgaHJlZj0iaHR0cDovL2tyb25rbHRkLm5ldC9kdWNrIi8-CiAgPGxpbmsgcmVsPSJhdmF0YXIiIHR5cGU9ImltYWdlL3BuZyIgbWVkaWE6d2lkdGg9Ijk2IiBt
ZWRpYTpoZWlnaHQ9Ijk2IiBocmVmPSJodHRwOi8va3JvbmtsdGQubmV0L3RoZW1lL2NsZWFuZXIvZGVmYXVsdC1hdmF0YXItcHJvZmlsZS5wbmciLz4KICA8bGluayByZWw9ImF2YXRhc
iIgdHlwZT0iaW1hZ2UvcG5nIiBtZWRpYTp3aWR0aD0iNDgiIG1lZGlhOmhlaWdodD0iNDgiIGhyZWY9Imh0dHA6Ly9rcm9ua2x0ZC5uZXQvdGhlbWUvY2xlYW5lci9kZWZhdWx0LWF2YX
Rhci1zdHJlYW0ucG5nIi8-CiAgPGxpbmsgcmVsPSJhdmF0YXIiIHR5cGU9ImltYWdlL3BuZyIgbWVkaWE6d2lkdGg9IjI0IiBtZWRpYTpoZWlnaHQ9IjI0IiBocmVmPSJodHRwOi8va3J
vbmtsdGQubmV0L3RoZW1lL2NsZWFuZXIvZGVmYXVsdC1hdmF0YXItbWluaS5wbmciLz4KICA8cG9jbzpwcmVmZXJyZWRVc2VybmFtZT5kdWNrPC9wb2NvOnByZWZlcnJlZFVzZXJuYW1l
PgogIDxwb2NvOmRpc3BsYXlOYW1lPkRhbmllbCBSZW5mZXI8L3BvY286ZGlzcGxheU5hbWU-CiA8L2F1dGhvcj4KIDwhLS1EZXByZWNhdGlvbiB3YXJuaW5nOiBhY3Rpdml0eTphY3Rvc
iBpcyBwcmVzZW50IG9ubHkgZm9yIGJhY2t3YXJkIGNvbXBhdGliaWxpdHkuIEl0IHdpbGwgYmUgcmVtb3ZlZCBpbiB0aGUgbmV4dCB2ZXJzaW9uIG9mIFN0YXR1c05ldC4tLT4KIDxhY3
Rpdml0eTphY3Rvcj4KICA8YWN0aXZpdHk6b2JqZWN0LXR5cGU-aHR0cDovL2FjdGl2aXR5c3RyZWEubXMvc2NoZW1hLzEuMC9wZXJzb248L2FjdGl2aXR5Om9iamVjdC10eXBlPgogIDx
pZD5odHRwOi8va3JvbmtsdGQubmV0L3VzZXIvMTwvaWQ-CiAgPHRpdGxlPkRhbmllbCBSZW5mZXI8L3RpdGxlPgogIDxsaW5rIHJlbD0iYWx0ZXJuYXRlIiB0eXBlPSJ0ZXh0L2h0bWwi
IGhyZWY9Imh0dHA6Ly9rcm9ua2x0ZC5uZXQvZHVjayIvPgogIDxsaW5rIHJlbD0iYXZhdGFyIiB0eXBlPSJpbWFnZS9wbmciIG1lZGlhOndpZHRoPSI5NiIgbWVkaWE6aGVpZ2h0PSI5N
iIgaHJlZj0iaHR0cDovL2tyb25rbHRkLm5ldC90aGVtZS9jbGVhbmVyL2RlZmF1bHQtYXZhdGFyLXByb2ZpbGUucG5nIi8-CiAgPGxpbmsgcmVsPSJhdmF0YXIiIHR5cGU9ImltYWdlL3
BuZyIgbWVkaWE6d2lkdGg9IjQ4IiBtZWRpYTpoZWlnaHQ9IjQ4IiBocmVmPSJodHRwOi8va3JvbmtsdGQubmV0L3RoZW1lL2NsZWFuZXIvZGVmYXVsdC1hdmF0YXItc3RyZWFtLnBuZyI
vPgogIDxsaW5rIHJlbD0iYXZhdGFyIiB0eXBlPSJpbWFnZS9wbmciIG1lZGlhOndpZHRoPSIyNCIgbWVkaWE6aGVpZ2h0PSIyNCIgaHJlZj0iaHR0cDovL2tyb25rbHRkLm5ldC90aGVtZS9jbGVhbmVyL2RlZmF1bHQtYXZhdGFyLW1pbmkucG5nIi8-CiAgPHBvY286cHJlZmVycmVkVXNlcm5hbWU-ZHVjazwvcG9jbzpwcmVmZXJyZWRVc2VybmFtZT4KICA8cG9jbzpkaXNwbGF5TmFtZT5EYW5pZWwgUmVuZmVyPC9wb2NvOmRpc3BsYXlOYW1lPgogPC9hY3Rpdml0eTphY3Rvcj4KPC9lbnRyeT4K"})





 (def val-env2
   {:sig
    "H9YxLGdyCTmTSMb9XrM5qZamUkDezrZQmWEBFMv0A949HPSg6Ex9a1VCG8ORzttYR9dJU0dRCqNsa8HzGPUFGRZEBNKJOJH1pPIuyA2R_zcqy9C-zPwO-ka6k4YEcbIycUd3uqnwo--56kGNDnq_W7kMhT4Ms-9wZxbbx2_vZy0=",
    :data
    "PD94bWwgdmVyc2lvbj0iMS4wIiBlbmNvZGluZz0iVVRGLTgiID8-PGVudHJ5IHhtbG5zPSJodHRwOi8vd3d3LnczLm9yZy8yMDA1L0F0b20iIHhtbG5zOnRocj0iaHR0cDovL3B1cmwub3JnL3N5bmRpY2F0aW9uL3RocmVhZC8xLjAiIHhtbG5zOmFjdGl2aXR5PSJodHRwOi8vYWN0aXZpdHlzdHJlYS5tcy9zcGVjLzEuMC8iIHhtbG5zOmdlb3Jzcz0iaHR0cDovL3d3dy5nZW9yc3Mub3JnL2dlb3JzcyIgeG1sbnM6b3N0YXR1cz0iaHR0cDovL29zdGF0dXMub3JnL3NjaGVtYS8xLjAiIHhtbG5zOnBvY289Imh0dHA6Ly9wb3J0YWJsZWNvbnRhY3RzLm5ldC9zcGVjLzEuMCIgeG1sbnM6bWVkaWE9Imh0dHA6Ly9wdXJsLm9yZy9zeW5kaWNhdGlvbi9hdG9tbWVkaWEiIHhtbG5zOnN0YXR1c25ldD0iaHR0cDovL3N0YXR1cy5uZXQvc2NoZW1hL2FwaS8xLyI-CiA8YWN0aXZpdHk6b2JqZWN0LXR5cGU-aHR0cDovL2FjdGl2aXR5c3RyZWEubXMvc2NoZW1hLzEuMC9ub3RlPC9hY3Rpdml0eTpvYmplY3QtdHlwZT4KIDxpZD5odHRwOi8va3JvbmtsdGQubmV0L25vdGljZS82PC9pZD4KIDx0aXRsZT5AZGFuaWVsQHJlbmZlci5uYW1lIHRlc3Q8L3RpdGxlPgogPGNvbnRlbnQgdHlwZT0iaHRtbCI-QCZsdDtzcGFuIGNsYXNzPSZxdW90O3ZjYXJkJnF1b3Q7Jmd0OyZsdDthIGhyZWY9JnF1b3Q7aHR0cDovL3JlbmZlci5uYW1lL2RhbmllbCZxdW90OyBjbGFzcz0mcXVvdDt1cmwmcXVvdDsmZ3Q7Jmx0O3NwYW4gY2xhc3M9JnF1b3Q7Zm4gbmlja25hbWUmcXVvdDsmZ3Q7ZGFuaWVsQHJlbmZlci5uYW1lJmx0Oy9zcGFuJmd0OyZsdDsvYSZndDsmbHQ7L3NwYW4mZ3Q7IHRlc3Q8L2NvbnRlbnQ-CiA8bGluayByZWw9ImFsdGVybmF0ZSIgdHlwZT0idGV4dC9odG1sIiBocmVmPSJodHRwOi8va3JvbmtsdGQubmV0L25vdGljZS82Ii8-CiA8YWN0aXZpdHk6dmVyYj5odHRwOi8vYWN0aXZpdHlzdHJlYS5tcy9zY2hlbWEvMS4wL3Bvc3Q8L2FjdGl2aXR5OnZlcmI-CiA8cHVibGlzaGVkPjIwMTEtMTItMDNUMjA6MTc6MTYrMDA6MDA8L3B1Ymxpc2hlZD4KIDx1cGRhdGVkPjIwMTEtMTItMDNUMjA6MTc6MTYrMDA6MDA8L3VwZGF0ZWQ-CiA8YXV0aG9yPgogIDxhY3Rpdml0eTpvYmplY3QtdHlwZT5odHRwOi8vYWN0aXZpdHlzdHJlYS5tcy9zY2hlbWEvMS4wL3BlcnNvbjwvYWN0aXZpdHk6b2JqZWN0LXR5cGU-CiAgPHVyaT5odHRwOi8va3JvbmtsdGQubmV0L3VzZXIvMTwvdXJpPgogIDxuYW1lPmR1Y2s8L25hbWU-CiAgPGxpbmsgcmVsPSJhbHRlcm5hdGUiIHR5cGU9InRleHQvaHRtbCIgaHJlZj0iaHR0cDovL2tyb25rbHRkLm5ldC9kdWNrIi8-CiAgPGxpbmsgcmVsPSJhdmF0YXIiIHR5cGU9ImltYWdlL2pwZWciIG1lZGlhOndpZHRoPSIyMDAiIG1lZGlhOmhlaWdodD0iMjAwIiBocmVmPSJodHRwOi8va3JvbmtsdGQubmV0L2F2YXRhci8xLTIwMC0yMDExMTEyNTE3NDgxOS5qcGVnIi8-CiAgPGxpbmsgcmVsPSJhdmF0YXIiIHR5cGU9ImltYWdlL2pwZWciIG1lZGlhOndpZHRoPSI5NiIgbWVkaWE6aGVpZ2h0PSI5NiIgaHJlZj0iaHR0cDovL2tyb25rbHRkLm5ldC9hdmF0YXIvMS05Ni0yMDExMTEyNTE3NDgyMC5qcGVnIi8-CiAgPGxpbmsgcmVsPSJhdmF0YXIiIHR5cGU9ImltYWdlL2pwZWciIG1lZGlhOndpZHRoPSI0OCIgbWVkaWE6aGVpZ2h0PSI0OCIgaHJlZj0iaHR0cDovL2tyb25rbHRkLm5ldC9hdmF0YXIvMS00OC0yMDExMTEyNTE3NDgyMC5qcGVnIi8-CiAgPGxpbmsgcmVsPSJhdmF0YXIiIHR5cGU9ImltYWdlL2pwZWciIG1lZGlhOndpZHRoPSIyNCIgbWVkaWE6aGVpZ2h0PSIyNCIgaHJlZj0iaHR0cDovL2tyb25rbHRkLm5ldC9hdmF0YXIvMS0yNC0yMDExMTEyNTE3NDgyMC5qcGVnIi8-CiAgPGdlb3Jzczpwb2ludD40Mi4zMTg5MyAtODMuMzgwNzc8L2dlb3Jzczpwb2ludD4KICA8cG9jbzpwcmVmZXJyZWRVc2VybmFtZT5kdWNrPC9wb2NvOnByZWZlcnJlZFVzZXJuYW1lPgogIDxwb2NvOmRpc3BsYXlOYW1lPkRhbmllbCBSZW5mZXI8L3BvY286ZGlzcGxheU5hbWU-CiAgPHBvY286YWRkcmVzcz4KICAgPHBvY286Zm9ybWF0dGVkPldlc3RsYW5kLCBNSTwvcG9jbzpmb3JtYXR0ZWQ-CiAgPC9wb2NvOmFkZHJlc3M-CiAgPHBvY286dXJscz4KICAgPHBvY286dHlwZT5ob21lcGFnZTwvcG9jbzp0eXBlPgogICA8cG9jbzp2YWx1ZT5odHRwOi8va3JvbmtsdGQubmV0PC9wb2NvOnZhbHVlPgogICA8cG9jbzpwcmltYXJ5PnRydWU8L3BvY286cHJpbWFyeT4KICA8L3BvY286dXJscz4KICA8c3RhdHVzbmV0OnByb2ZpbGVfaW5mbyBsb2NhbF9pZD0iMSIgZm9sbG93aW5nPSJ0cnVlIiBibG9ja2luZz0iZmFsc2UiPjwvc3RhdHVzbmV0OnByb2ZpbGVfaW5mbz4KIDwvYXV0aG9yPgogPCEtLURlcHJlY2F0aW9uIHdhcm5pbmc6IGFjdGl2aXR5OmFjdG9yIGlzIHByZXNlbnQgb25seSBmb3IgYmFja3dhcmQgY29tcGF0aWJpbGl0eS4gSXQgd2lsbCBiZSByZW1vdmVkIGluIHRoZSBuZXh0IHZlcnNpb24gb2YgU3RhdHVzTmV0Li0tPgogPGFjdGl2aXR5OmFjdG9yPgogIDxhY3Rpdml0eTpvYmplY3QtdHlwZT5odHRwOi8vYWN0aXZpdHlzdHJlYS5tcy9zY2hlbWEvMS4wL3BlcnNvbjwvYWN0aXZpdHk6b2JqZWN0LXR5cGU-CiAgPGlkPmh0dHA6Ly9rcm9ua2x0ZC5uZXQvdXNlci8xPC9pZD4KICA8dGl0bGU-RGFuaWVsIFJlbmZlcjwvdGl0bGU-CiAgPGxpbmsgcmVsPSJhbHRlcm5hdGUiIHR5cGU9InRleHQvaHRtbCIgaHJlZj0iaHR0cDovL2tyb25rbHRkLm5ldC9kdWNrIi8-CiAgPGxpbmsgcmVsPSJhdmF0YXIiIHR5cGU9ImltYWdlL2pwZWciIG1lZGlhOndpZHRoPSIyMDAiIG1lZGlhOmhlaWdodD0iMjAwIiBocmVmPSJodHRwOi8va3JvbmtsdGQubmV0L2F2YXRhci8xLTIwMC0yMDExMTEyNTE3NDgxOS5qcGVnIi8-CiAgPGxpbmsgcmVsPSJhdmF0YXIiIHR5cGU9ImltYWdlL2pwZWciIG1lZGlhOndpZHRoPSI5NiIgbWVkaWE6aGVpZ2h0PSI5NiIgaHJlZj0iaHR0cDovL2tyb25rbHRkLm5ldC9hdmF0YXIvMS05Ni0yMDExMTEyNTE3NDgyMC5qcGVnIi8-CiAgPGxpbmsgcmVsPSJhdmF0YXIiIHR5cGU9ImltYWdlL2pwZWciIG1lZGlhOndpZHRoPSI0OCIgbWVkaWE6aGVpZ2h0PSI0OCIgaHJlZj0iaHR0cDovL2tyb25rbHRkLm5ldC9hdmF0YXIvMS00OC0yMDExMTEyNTE3NDgyMC5qcGVnIi8-CiAgPGxpbmsgcmVsPSJhdmF0YXIiIHR5cGU9ImltYWdlL2pwZWciIG1lZGlhOndpZHRoPSIyNCIgbWVkaWE6aGVpZ2h0PSIyNCIgaHJlZj0iaHR0cDovL2tyb25rbHRkLm5ldC9hdmF0YXIvMS0yNC0yMDExMTEyNTE3NDgyMC5qcGVnIi8-CiAgPGdlb3Jzczpwb2ludD40Mi4zMTg5MyAtODMuMzgwNzc8L2dlb3Jzczpwb2ludD4KICA8cG9jbzpwcmVmZXJyZWRVc2VybmFtZT5kdWNrPC9wb2NvOnByZWZlcnJlZFVzZXJuYW1lPgogIDxwb2NvOmRpc3BsYXlOYW1lPkRhbmllbCBSZW5mZXI8L3BvY286ZGlzcGxheU5hbWU-CiAgPHBvY286YWRkcmVzcz4KICAgPHBvY286Zm9ybWF0dGVkPldlc3RsYW5kLCBNSTwvcG9jbzpmb3JtYXR0ZWQ-CiAgPC9wb2NvOmFkZHJlc3M-CiAgPHBvY286dXJscz4KICAgPHBvY286dHlwZT5ob21lcGFnZTwvcG9jbzp0eXBlPgogICA8cG9jbzp2YWx1ZT5odHRwOi8va3JvbmtsdGQubmV0PC9wb2NvOnZhbHVlPgogICA8cG9jbzpwcmltYXJ5PnRydWU8L3BvY286cHJpbWFyeT4KICA8L3BvY286dXJscz4KICA8c3RhdHVzbmV0OnByb2ZpbGVfaW5mbyBsb2NhbF9pZD0iMSIgZm9sbG93aW5nPSJ0cnVlIiBibG9ja2luZz0iZmFsc2UiPjwvc3RhdHVzbmV0OnByb2ZpbGVfaW5mbz4KIDwvYWN0aXZpdHk6YWN0b3I-CiA8bGluayByZWw9Im9zdGF0dXM6Y29udmVyc2F0aW9uIiBocmVmPSJodHRwOi8va3JvbmtsdGQubmV0L2NvbnZlcnNhdGlvbi81Ii8-CiA8bGluayByZWw9Im9zdGF0dXM6YXR0ZW50aW9uIiBocmVmPSJhY2N0OmRhbmllbEByZW5mZXIubmFtZSIvPgogPGxpbmsgcmVsPSJtZW50aW9uZWQiIGhyZWY9ImFjY3Q6ZGFuaWVsQHJlbmZlci5uYW1lIi8-CiA8Z2VvcnNzOnBvaW50PjQyLjMyNDIgLTgzLjQwMDIxPC9nZW9yc3M6cG9pbnQ-CiA8c291cmNlPgogIDxpZD5odHRwOi8vcmVuZmVyLm5hbWUvYXBpL3N0YXR1c2VzL3VzZXJfdGltZWxpbmUvNGVjMzQ0ZDFkMmVmNjBiNzcyNWE2Y2VjLmF0b208L2lkPgogIDx0aXRsZT5EYW5pZWwgRS4gUmVuZmVyPC90aXRsZT4KICA8bGluayByZWw9ImFsdGVybmF0ZSIgdHlwZT0idGV4dC9odG1sIiBocmVmPSJodHRwOi8vcmVuZmVyLm5hbWUvZGFuaWVsIi8-CiAgPGxpbmsgcmVsPSJzZWxmIiB0eXBlPSJhcHBsaWNhdGlvbi9hdG9tK3htbCIgaHJlZj0iaHR0cDovL3JlbmZlci5uYW1lL2FwaS9zdGF0dXNlcy91c2VyX3RpbWVsaW5lLzRlYzM0NGQxZDJlZjYwYjc3MjVhNmNlYy5hdG9tIi8-CiAgPGljb24-aHR0cDovL2tyb25rbHRkLm5ldC90aGVtZS9jbGVhbmVyL2RlZmF1bHQtYXZhdGFyLXByb2ZpbGUucG5nPC9pY29uPgogPC9zb3VyY2U-CiA8bGluayByZWw9InNlbGYiIHR5cGU9ImFwcGxpY2F0aW9uL2F0b20reG1sIiBocmVmPSJodHRwOi8va3JvbmtsdGQubmV0L2FwaS9zdGF0dXNlcy9zaG93LzYuYXRvbSIvPgogPGxpbmsgcmVsPSJlZGl0IiB0eXBlPSJhcHBsaWNhdGlvbi9hdG9tK3htbCIgaHJlZj0iaHR0cDovL2tyb25rbHRkLm5ldC9hcGkvc3RhdHVzZXMvc2hvdy82LmF0b20iLz4KIDxzdGF0dXNuZXQ6bm90aWNlX2luZm8gbG9jYWxfaWQ9IjYiIHNvdXJjZT0id2ViIj48L3N0YXR1c25ldDpub3RpY2VfaW5mbz4KPC9lbnRyeT4K",
    :alg "RSA-SHA256",
    :encoding "base64url"}


   )



 (def test-public-key
   (str "RSA.mVgY8RN6URBTstndvmUUPb4UZTdwvwmddSKE5z_jvKUEK6yk1u3rrC9yN8k6FilGj9K0eeUPe2hf4Pj-5CmHww=="
        ".AQAB"))

 (def test-private-key
   (str test-public-key
        ".Lgy_yL3hsLBngkFdDw1Jy9TmSRMiH6yihYetQ8jy-jZXdsZXd8V5ub3kuBHHk4M39i3TduIkcrjcsiWQb77D8Q=="))

 (def test-atom
   "<?xml version='1.0' encoding='UTF-8'?>
    <entry xmlns='http://www.w3.org/2005/Atom'>
    <id>tag:example.com,2009:cmt-0.44775718</id>
      <author><name>test@example.com</name><uri>acct:test@example.com</uri>
      </author>
      <content>Salmon swim upstream!</content>
      <title>Salmon swim upstream!</title>
      <updated>2009-12-18T20:04:03Z</updated>
    </entry>")



 (defn valid-envelope-stream
   []
   (io/input-stream (io/resource "envelope.xml")))

 (defn byte-array?
   "Returns true if the object is a byte array"
   [o]
   (= (type o) (type (byte-array []))))


 ;; Taken from the python tests
 (fact "#'normalize-user-id"
   (let [id1 "http://example.com"
         id2 "https://www.example.org/bob"
         id3 "acct:bob@example.org"
         em3 "bob@example.org"]
     (fact "http urls are unaltered"
       (normalize-user-id id1) => id1)
     (fact "https urls are unaltered"
       (normalize-user-id id2) => id2)
     (fact "acct uris are unaltered"
       (normalize-user-id id3) => id3)
     (fact "email addresses have the acct scheme appended"
       (normalize-user-id em3) => id3)))

 (fact "#'get-key"
   (future-fact "when the user does not have a key"
       (fact "should return nil"
         (let [user (actions.user/create (factory User {:discovered true}))]
           (get-key user)) => nil))
   
   (fact "when there is a key"
     (fact "should return a key"
       ;; TODO: explicitly assign key
       (let [user (actions.user/register
                   (factory User {:discovered true :password "hunter2"}))]
         (Thread/sleep 2000)
         (get-key user) => (partial instance? Key)))))

 (fact "#'signature-valid?"
   (fact "when it is valid"
     (fact "should return truthy"
       (let [envelope val-env #_(stream->envelope (valid-envelope-stream))
             key (model.signature/get-key-from-armored
                  {:armored-n armored-n
                   :armored-e armored-e})]
         (signature-valid? envelope key) => truthy))))

 (fact "#'decode-envelope"
   (fact "should return a string"
     (let [envelope (stream->envelope (valid-envelope-stream))]
       (decode-envelope envelope) => string?)))

 (fact "#'extract-activity"
   (fact "should return an activity"
     (let [envelope (stream->envelope (valid-envelope-stream))]
       (extract-activity envelope)) => model/activity?))

 (fact "#'stream->envelope"
   (fact "should return an envelope"
     (stream->envelope (valid-envelope-stream)) => map?))

 (future-fact "#'process"
   (fact "with a valid signature"
     (fact "should create the message"
       (let [envelope (-> (valid-envelope-stream) stream->envelope)
             user (-> envelope extract-activity
                      helpers.activity/get-author)]
         (actions.user/discover user)
         (let [sig (:sig envelope)
               n "1PAkgCMvhHGg-rqBDdaEilXCi0b2EyO-JwSkZqjgFK5HrS0vy4Sy8l3CYbcLxo6d3QG_1SbxtlFoUo4HsbMTrDtV7yNlIJlcsbWFWkT3H4BZ1ioNqPQOKeLIT5ZZXfSWCiIs5PM1H7pSOlaItn6nw92W53205YXyHKHmZWqDpO0="
               e "AQAB"]
           (model.signature/set-armored-key (:_id user) n e)
           (process user envelope) => truthy
           (provided
             (actions.activity/remote-create anything) => truthy :called 1)))))))

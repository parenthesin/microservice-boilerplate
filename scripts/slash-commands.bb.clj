#!/usr/bin/env bb

(require '[babashka.curl :as curl]
         '[cheshire.core :as json])

(def bot-token "<TOKEN>")
(def application-id "<APP-ID>")
(def commands-url (str "https://discord.com/api/v8/applications/" application-id "/commands"))

(def commands
  (json/encode
   [{:name "roll"
     :description "Roll dices, example: /roll 3D6+3"
     :options [{:name "dice"
                :description "<NDM> N = Number of dices D = Type of dices (D6, D12, D20) M = Modifiers (+1, -3), Example: 3D6+3"
                :type 3
                :required true}]}]))

(defn upsert-commands []
  (curl/put commands-url {:headers {"Accept" "application/json"
                                     "Content-Type" "application/json"
                                     "Authorization" (str "Bot " bot-token)}
                           :body commands
                           :throw false}))

(-> commands-url
    (curl/get {:headers {"Accept" "application/json"
                         "Content-Type" "application/json"
                         "Authorization" (str "Bot " bot-token)}
               :throw false})
    :body
    (json/decode true)
    first)

(comment
  (upsert-commands))

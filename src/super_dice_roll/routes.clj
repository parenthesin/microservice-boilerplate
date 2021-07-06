(ns super-dice-roll.routes
  (:require [reitit.swagger :as swagger]
            [schema.core :as s]
            [super-dice-roll.discord.interceptor :as discord.interceptor]
            [super-dice-roll.discord.ports.http-in :as discord.ports.http-in]
            [super-dice-roll.discord.schemas.http-in :as discord.schemas.http-in]))

(def routes
  [["/swagger.json"
    {:get {:no-doc true
           :swagger {:info {:title "super-dice-roll"
                            :description "Bot to roll dices."}}
           :handler (swagger/create-swagger-handler)}}]

   ["/discord"
    {:swagger {:tags ["discord"]}
     :interceptors [(discord.interceptor/authentication-interceptor)]
     :parameters {:header {:x-signature-ed25519 s/Str
                           :x-signature-timestamp s/Str}}}

    ["/webhook"
     {:post {:summary "Discord webhook-based interactions."
             :parameters {:body discord.schemas.http-in/InteractionRequest}
             :responses {200 {:body discord.schemas.http-in/InteractionResponse}
                         400 {:body s/Str}
                         401 {:body s/Str}
                         500 {:body s/Str}}
             :handler discord.ports.http-in/process-interaction!}}]]])

(ns super-dice-roll.discord.schemas.http-in
  (:require [schema.core :as s]))

(def InteractionRequestTypeDefinition
  {1 :ping
   2 :application-command
   3 :message-component})
(def InteractionRequestType (apply s/enum (keys InteractionRequestTypeDefinition)))

(s/defschema InteractionRequest
  {:id s/Str
   :application_id s/Str
   :type InteractionRequestType
   ;(s/optional-key :data) InteractionRequestData
   (s/optional-key :guild_id) s/Str
   (s/optional-key :channel_id) s/Str
   ;(s/optional-key :member) InteractionRequestMember
   ;(s/optional-key :user) InteractionRequestUser
   :token s/Str
   :version s/Int
   ;(s/optional-key :message) InteractionRequestMessage
   s/Any s/Any})

(def InteractionResponseTypeDefinition
  {1 :pong
   4 :channel-message-with-source
   5 :deferred-channel-message-with-source
   6 :deferred-update-message
   7 :update-message})
(def InteractionResponseType (apply s/enum (keys InteractionResponseTypeDefinition)))

(s/defschema InteractionResponseData
  {(s/optional-key :tts) s/Bool
   (s/optional-key :content) s/Str
   (s/optional-key :flags) s/Int
   (s/optional-key :allowed_mentions) {:parse [s/Str]}
   ;(s/optional-key :components) [InteractionResponseComponent]
   ;(s/optional-key :embeds) [InteractionResponseEmbed]
   s/Any s/Any})

(s/defschema InteractionResponse
  {:type InteractionResponseType
   (s/optional-key :data) InteractionResponseData})

(ns super-dice-roll.discord.schemas.http-in
  (:require [schema.core :as s]))

(def InteractionRequestTypeDefinition
  {1 :ping
   2 :application-command
   3 :message-component})
(def InteractionRequestType (apply s/enum (keys InteractionRequestTypeDefinition)))

(def InteractionRequestDataOptionTypeDefinition
  {1 :sub-command
   2 :sub-command-group
   3 :string
   4 :integer
   5 :boolean
   6 :user
   7 :channel
   8 :role
   9 :mentionable})
(def InteractionRequestDataOptionType (apply s/enum (keys InteractionRequestDataOptionTypeDefinition)))

(s/defschema InteractionRequestDataOptionItem
  {:name s/Str
   :type InteractionRequestDataOptionType
   (s/optional-key :value) s/Str
   s/Any s/Any})

(s/defschema InteractionRequestDataOption
  {:name s/Str
   :type InteractionRequestDataOptionType
   (s/optional-key :value) s/Str
   (s/optional-key :options) [InteractionRequestDataOptionItem]
   s/Any s/Any})

(s/defschema InteractionRequestData
  {:id s/Str
   :name s/Str
   :options [InteractionRequestDataOption]
   s/Any s/Any})

(s/defschema InteractionRequestUser
  {:id s/Str
   :username s/Str
   s/Any s/Any})

(s/defschema InteractionRequestMember
  {(s/optional-key :user) InteractionRequestUser
   (s/optional-key :nick) (s/maybe s/Str)
   s/Any s/Any})

(s/defschema InteractionRequest
  {:id s/Str
   :application_id s/Str
   :type InteractionRequestType
   (s/optional-key :data) InteractionRequestData
   (s/optional-key :guild_id) s/Str
   (s/optional-key :channel_id) s/Str
   (s/optional-key :member) InteractionRequestMember
   (s/optional-key :user) InteractionRequestUser
   :token s/Str
   :version s/Int
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

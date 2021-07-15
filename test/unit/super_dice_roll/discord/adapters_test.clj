(ns unit.super-dice-roll.discord.adapters-test
  (:require [clojure.test :refer [deftest is testing use-fixtures]]
            [clojure.test.check.clojure-test :refer [defspec]]
            [clojure.test.check.properties :as properties]
            [matcher-combinators.test :refer [match?]]
            [schema-generators.generators :as g]
            [schema.core :as s]
            [schema.test :as schema.test]
            [super-dice-roll.discord.adapters :as adapters]
            [super-dice-roll.messages :as messages]
            [super-dice-roll.schemas.models :as schemas.models]))

(use-fixtures :once schema.test/validate-schemas)

(def guild-request-1 {:guild_id "853711282723880895"
                      :type 2
                      :channel_id "85371128272388091"
                      :member {:deaf false
                               :nick nil
                               :permissions "137438953564"
                               :pending false
                               :premium_since nil
                               :roles ["853711852508151906"]
                               :is_pending false
                               :avatar nil
                               :joined_at "2021-06-13T19:04:35.870000+00:00"
                               :user {:username "dombelombers"
                                      :id "598978693322375444"
                                      :avatar "42fe6008314f0f977ee9e9166cf261ff"
                                      :public_flags 0
                                      :discriminator "4261"}
                               :mute false}
                      :token "aW50ZXJhY3Rpb246ODYzNDEzMDA1NTc0OTk1OTc5OlBGaTg1"
                      :id "863413005574996100"
                      :application_id "861964097700757703"
                      :version 1
                      :data {:name "roll"
                             :id "86212849198956528"
                             :options [{:name "dice"
                                        :value "3d6+1"
                                        :type 3}]}})

(deftest wire-in->model-test
  (testing "should reduce and get totals for wallet entries and current usd"
    (is (match? {:user {:id "598978693322375444"
                        :username "dombelombers"
                        :nick ""}
                 :command "3d6+1"}
                (adapters/wire-in->model guild-request-1)))))

(deftest rolled->message-test
  (testing "adapt rolled results into output message"
    (is (= "*nicola rolled 2d12+5*\n`[4,7] + 5`\n**total: 16**\n"
           (adapters/rolled->message {:roll {:command {:user {:id "12345678"
                                                              :username "usernola"
                                                              :nick "nicola"},
                                                       :command "2d12+5"}
                                             :times 2
                                             :dice 12
                                             :modifier 5}
                                      :results [4 7]
                                      :total 16}))
        "should show show nick and modifier")
    (is (= "*usernola rolled 2d12+5*\n`[4,7] + 5`\n**total: 16**\n"
           (adapters/rolled->message {:roll {:command {:user {:id "12345678"
                                                              :username "usernola"
                                                              :nick nil},
                                                       :command "2d12+5"}
                                             :times 2
                                             :dice 12
                                             :modifier 5}
                                      :results [4 7]
                                      :total 16}))
        "should show show username and modifier")
    (is (= "*usernola rolled 2d12+5*\n`[4,7]`\n**total: 11**\n"
           (adapters/rolled->message {:roll {:command {:user {:id "12345678"
                                                              :username "usernola"
                                                              :nick nil},
                                                       :command "2d12+5"}
                                             :times 2
                                             :dice 12
                                             :modifier 0}
                                      :results [4 7]
                                      :total 11}))
        "should show show nick and no modifier")))

(defspec rolled-message-generative-test 50
  (properties/for-all [rolled (g/generator schemas.models/Rolled)]
    (s/validate s/Str (adapters/rolled->message rolled))))

(deftest roll-command->error-message-test
  (testing "adapt roll command into error message"
    (is (= (str "wararana the command *wreberwreber* is invalid\n" messages/help-roll)
           (adapters/roll-command->error-message {:user {:id "123456789"
                                                         :username "dombelombers"
                                                         :nick "wararana"},
                                                  :command "wreberwreber"}))
        "should show show nick and error")
    (is (= (str "dombelombers the command *wreberwreber* is invalid\n" messages/help-roll)
           (adapters/roll-command->error-message {:user {:id "123456789"
                                                         :username "dombelombers"
                                                         :nick ""},
                                                  :command "wreberwreber"}))
        "should show show username and error")))

(defspec roll-command-error-message-generative-test 50
  (properties/for-all [roll-cmd (g/generator schemas.models/RollCommand)]
    (s/validate s/Str (adapters/roll-command->error-message roll-cmd))))

(ns unit.super-dice-roll.discord.security-test
  (:require [clojure.test :refer [deftest is testing use-fixtures]]
            [matcher-combinators.test :refer [match?]]
            [schema.test :as schema.test]
            [super-dice-roll.discord.security :as discord.security]))

(use-fixtures :once schema.test/validate-schemas)

(deftest verify-request-test
  (let [key-pair (discord.security/generate-keypair)
        signer (discord.security/new-signer (:private key-pair))
        public-key-hex (discord.security/bytes->hex (.getEncoded (:public key-pair)))
        timestamp "1625603592"
        body "this should be a json."
        signature (->> (str timestamp body) .getBytes (discord.security/sign signer) discord.security/bytes->hex)]
    (testing "verify-request should check signature vs public-key, timestamp and body"
      (is (match? true
                  (discord.security/verify-request public-key-hex
                                                   timestamp
                                                   body
                                                   signature)))
      (is (match? false
                  (discord.security/verify-request public-key-hex
                                                   timestamp
                                                   "this should be a json.hacks"
                                                   signature))))))

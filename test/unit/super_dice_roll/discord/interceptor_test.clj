(ns unit.super-dice-roll.discord.interceptor-test
  (:require [clojure.java.io :as io]
            [clojure.test :refer [deftest is testing use-fixtures]]
            [matcher-combinators.test :refer [match?]]
            [schema.test :as schema.test]
            [super-dice-roll.discord.interceptor :as discord.interceptor]
            [super-dice-roll.discord.security :as discord.security]))

(use-fixtures :once schema.test/validate-schemas)

(def interceptor-fn (-> (discord.interceptor/authentication-interceptor) :enter))
(defn build-ctx [public-key timestamp body signature]
  {:response {:status 200}
   :request {:components {:config {:config {:discord {:app-public-key public-key}}}}
             :headers {"x-signature-ed25519" signature
                       "x-signature-timestamp" timestamp}}
   :raw-body (io/input-stream (.getBytes body))})

(deftest verify-request-test
  (let [key-pair (discord.security/generate-keypair)
        signer (discord.security/new-signer (:private key-pair))
        public-key-hex (discord.security/bytes->hex (.getEncoded (:public key-pair)))
        timestamp "1625603592"
        body "this should be a json."
        signature (->> (str timestamp body) .getBytes (discord.security/sign signer) discord.security/bytes->hex)]
    (testing "interceptor should check signature vs public-key, timestamp and body"
      (is (match? {:response {:status 200}}
                  (interceptor-fn (build-ctx public-key-hex
                                             timestamp
                                             body
                                             signature))))
      (is (match? {:response {:status 401}}
                  (interceptor-fn (build-ctx public-key-hex
                                             timestamp
                                             (str body "hackedbody")
                                             signature)))))))

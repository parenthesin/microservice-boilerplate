(ns super-dice-roll.discord.security
  (:require [clojure.string :as str])
  (:import (java.security SecureRandom)
           (org.bouncycastle.crypto.generators Ed25519KeyPairGenerator)
           (org.bouncycastle.crypto.params Ed25519KeyGenerationParameters Ed25519PrivateKeyParameters Ed25519PublicKeyParameters)
           (org.bouncycastle.crypto.signers Ed25519Signer)
           (org.bouncycastle.util.encoders Base64)))

(defn bytes->hex
  "convert byte array to hex string."
  [^bytes byte-array]
  (let [hex [\0 \1 \2 \3 \4 \5 \6 \7 \8 \9 \a \b \c \d \e \f]]
    (letfn [(hexify-byte [b]
              (let [v (bit-and b 0xFF)]
                [(hex (bit-shift-right v 4)) (hex (bit-and v 0x0F))]))]
      (str/join (mapcat hexify-byte byte-array)))))

(defn hex->bytes
  "convert hex string to byte array."
  [^String hex-string]
  (letfn [(unhexify-2 [^Character c1 ^Character c2]
            (unchecked-byte
             (+ (bit-shift-left (Character/digit c1 16) 4)
                (Character/digit c2 16))))]
    (byte-array (map #(apply unhexify-2 %) (partition 2 hex-string)))))

(defn generate-keypair
  "generate Ed25519 key pair.
  return {:private `Ed25519PrivateKeyParameters`
          :public `Ed25519PublicKeyParameters`}"
  []
  (let [random (SecureRandom.)
        kpg    (Ed25519KeyPairGenerator.)
        _ (.init kpg (Ed25519KeyGenerationParameters. random))
        key-pair (.generateKeyPair kpg)]
    {:private (cast Ed25519PrivateKeyParameters (.getPrivate key-pair))
     :public  (cast Ed25519PublicKeyParameters (.getPublic key-pair))}))

(defn new-signer
  "return new instance of `Ed25519Signer` initialized by private key"
  [private-key]
  (let [signer (Ed25519Signer.)]
    (.init signer true private-key)
    signer))

(defn sign
  "generate signature for msg byte array.
  return byte array with signature."
  [^Ed25519Signer signer msg-bytes]
  (.update signer msg-bytes 0 (alength msg-bytes))
  (.generateSignature signer))

(defn new-verifier
  "return new instance of `Ed25519Signer` initialized by public key."
  [public-key]
  (let [signer (Ed25519Signer.)]
    (.init signer false public-key)
    signer))

(defn verify
  "verify signature for msg byte array.
  return true if valid signature and false if not."
  [^Ed25519Signer signer msg-bytes signature]
  (.update signer msg-bytes 0 (alength msg-bytes))
  (.verifySignature signer signature))

(defn verify-request
  "verify discord payload with app public-key,
  request body, signature and timestamp headers"
  [public-key timestamp body signature]
  (verify (new-verifier (Ed25519PublicKeyParameters. (hex->bytes public-key) 0))
          (.getBytes (str timestamp body) "utf8")
          (hex->bytes signature)))

(comment
  (def kp (generate-keypair))
  (def s (new-signer (:private kp)))
  (def v (new-verifier (:public kp)))
  (def msg (.getBytes "Hello, world!"))

  (def signature (sign s msg))

  (bytes->hex signature)
  ;;"7abebcb847593e47a67438dee5a1a2af99dbbe90a2c643a6b7de99698d9a38a4ee478d7f20b2bb3e527282b73caf1fc7152989d0cff23f773b87570360537405"

  (String. (Base64/encode signature))
  ;;"uFs5Lw7W/dE92dIHutHbwPXzlArgkWfLXt7R8iWamfL8cr3zDKLiM7jFARLzNbYoKTgvRS5wB4a0Xm0BFl2ZAA=="

  (verify v msg signature)
  ;; true

  (def pl1 {:header {:sig "3d28da5389fe1bb740b8a9e370a86e0c33dfa6396a7bb04fc290078080a417865ec315af53d1314d7f8c5495a72819ee85dacf7ff094b6cc2a187ace7832a808", :time "1625603592"}, :public-key "db5b5a8d88a2918a03e52429cefc85cdd3a7f382d05b28cd411f40494dd26db6", :body "{\"application_id\":\"861964097700757534\",\"id\":\"862068692228243527\",\"token\":\"aW50ZXJhY3Rpb246ODYyMDY4NjkyMjI4MjQzNTI3OnFpZTlZUVFXQWlzSWdBbVBkZnJoT1Z2c2lDVkRxZ0doU3I0VXpUSUQ3WXN4S2JGNjdyUnhsNW01TlIzMnZnVHVWUlNYc0VqamtpN0xSQlFjbDJmcGdLeXFrSkNXOUM1cXZVMFl2QVV3dVpZOTYxdXlEMDNkblN4TXRMdHVKazFZ\",\"type\":1,\"user\":{\"avatar\":\"42fe6008311f0f977ee9e9166cf261ff\",\"discriminator\":\"4175\",\"id\":\"598978693322375169\",\"public_flags\":0,\"username\":\"delboni\"},\"version\":1}"})
  (def pl2 {:header {:sig "8cd610a038b0bbfb245c106962d787d84effaa402dab736d3ba74937f9418ba3e761a3064fe0b279bee1a6c1e2ce5f5a7f7a1497a37182b997ebfb3ebdf5df0f", :time "1625603594"}, :public-key "db5b5a8d88a2918a03e52429cefc85cdd3a7f382d05b28cd411f40494dd26db6", :body "{\"application_id\":\"861964097700757534\",\"id\":\"862068692228243526\",\"token\":\"aW50ZXJhY3Rpb246ODYyMDY4NjkyMjI4MjQzNTI2OmtKRlFpZ1lreGhPMTZaUzlrZkMxeGxUeGxDQVlUR1NxaDVXUkE0bTBCdHVlZWNKaHo5Q0VXSjhGSUhIQzFjZHZYQTU3Y1hpbjV3WlRIVHd1N3Z1MVlzVFNNUTNWbnRSOGV3TWs3cW5IWEhtS0VsUG1mYWJBZTI3cldoNnFnMWRZ\",\"type\":1,\"user\":{\"avatar\":\"42fe6008311f0f977ee9e9166cf261ff\",\"discriminator\":\"4175\",\"id\":\"598978693322375169\",\"public_flags\":0,\"username\":\"delboni\"},\"version\":1}"})

  (let [payload pl2
        public-key (get-in payload [:public-key])
        timestamp (get-in payload [:header :time])
        body (get-in payload [:body])
        signature (get-in payload [:header :sig])]
    (verify-request public-key timestamp body signature)))

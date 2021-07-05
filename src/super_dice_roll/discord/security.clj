(ns super-dice-roll.discord.security
  (:require [clojure.java.io :as io :refer [input-stream]]
            [clojure.string :as str])
  (:import (java.security SecureRandom Security)
           (org.bouncycastle.crypto.digests SHA3Digest)
           (org.bouncycastle.crypto.generators Ed25519KeyPairGenerator)
           (org.bouncycastle.crypto.params Ed25519KeyGenerationParameters Ed25519PrivateKeyParameters Ed25519PublicKeyParameters)
           (org.bouncycastle.crypto.signers Ed25519Signer)
           (org.bouncycastle.jce.provider BouncyCastleProvider)
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

(defn digest-stream
  "calculate SHA3-256 digest for given streaming input.
  As input may be:  File, URI, URL, Socket, byte array,
  or filename as String  which  will be coerced to BufferedInputStream and auto closed after.
  return digest as byte array."
  [input]
  (Security/addProvider (BouncyCastleProvider.))
  (with-open [in (input-stream input)]
    (let [buf (byte-array 1024)
          digest (SHA3Digest.)
          hash-buffer (byte-array (.getDigestSize digest))]
      (loop [n (.read in buf)]
        (if (<= n 0)
          (do (.doFinal digest hash-buffer 0) hash-buffer)
          (recur (do (.update digest buf 0 n) (.read in buf))))))))

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

(comment

  (def kp (generate-keypair))
  (def s (new-signer (:private kp)))
  (def v (new-verifier (:public kp)))
  (def msg (.getBytes "Hello, world!"))
  (def digest (digest-stream msg))

  (def signature (sign s digest))

  (bytes->hex signature)
  ;;"7abebcb847593e47a67438dee5a1a2af99dbbe90a2c643a6b7de99698d9a38a4ee478d7f20b2bb3e527282b73caf1fc7152989d0cff23f773b87570360537405"

  (String. (Base64/encode signature))
  ;;"uFs5Lw7W/dE92dIHutHbwPXzlArgkWfLXt7R8iWamfL8cr3zDKLiM7jFARLzNbYoKTgvRS5wB4a0Xm0BFl2ZAA=="

  (verify v (digest-stream msg) signature)
  ;; true
  )

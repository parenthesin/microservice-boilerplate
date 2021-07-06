(ns super-dice-roll.discord.security
  (:require [clojure.java.io :as io :refer [input-stream]]
            [clojure.string :as str])
  (:import (java.security SecureRandom Security)
           (org.bouncycastle.crypto.digests SHA3Digest)
           (org.bouncycastle.crypto.generators Ed25519KeyPairGenerator)
           (org.bouncycastle.crypto.params Ed25519KeyGenerationParameters Ed25519PrivateKeyParameters Ed25519PublicKeyParameters)
           (org.bouncycastle.crypto.signers Ed25519Signer)
           (org.bouncycastle.jce.provider BouncyCastleProvider)
           (org.bouncycastle.util.encoders Base64)

           (java.nio.charset StandardCharsets Charset UnsupportedCharsetException IllegalCharsetNameException CharsetEncoder)
           (java.util Arrays)
           (java.nio ByteBuffer CharBuffer)
           (java.io ByteArrayOutputStream ByteArrayInputStream InputStream)))

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

(defmacro if-let-all
  "Utility-macro - like `if-let`, but with multiple bindings that are all tested."
  {:style/indent 1}
  ([bindings then]
   `(if-let-all ~bindings ~then nil))
  ([bindings then else]
   (assert (vector? bindings))
   (let [amount (count bindings)]
     (assert (= (rem amount 2) 0))
     (assert (>= amount 2))
     `(if-let [~(first bindings) ~(second bindings)]
        ~(if (> amount 2)
           `(if-let-all ~(subvec bindings 2) ~then ~else)
           then)
        ~else))))

(defn hex->bytes-2
  "Converts the given string representing a hexadecimal number to a byte array.
  Each byte in the resulting array comes from 2 digits in the string.
  If the string cannot be converted, returns `nil`"
  ^bytes [^String hex-str]
  (let [len (count hex-str)

        result (byte-array (quot (inc len) 2))]
    (try
      (doseq [[i hex-part] (map-indexed vector (map (partial apply str) (partition-all 2 hex-str)))]
        (aset result i (unchecked-byte (Short/parseShort hex-part 16))))
      result
      (catch NumberFormatException _ nil))))

(defn read-all-bytes
  "Reads all bytes from either an `InputStream` or a `ByteBuffer`.
  If an `InputStream` is provided, it will be consumed, but not closed.
  Returns its result as a *new* byte array."
  ^bytes [input]
  (condp instance? input
    InputStream (let [bos (ByteArrayOutputStream.)]
                  (loop [next (.read ^InputStream input)]
                    (if (== next -1)
                      (.toByteArray bos)
                      (do
                        (.write bos next)
                        (recur (.read ^InputStream input))))))
    ByteBuffer (let [len (.remaining ^ByteBuffer input)
                     result (byte-array len)]
                 (.get ^ByteBuffer input result)
                 result)))

(defn encode
  "Encodes the given string to a byte array using the given charset/encoding.
  Returns `nil` if the charset is not available or if it doesn't support encoding."
  ^bytes [^String str ^String charset-name]
  (if-let-all [^Charset cs (try (Charset/forName charset-name) (catch UnsupportedCharsetException _ nil) (catch IllegalCharsetNameException _ nil))
               ^CharsetEncoder encoder (try (.newEncoder cs) (catch UnsupportedOperationException _ nil))]
              (when (.canEncode encoder str)
                (read-all-bytes (.encode encoder (CharBuffer/wrap str))))))

(comment

  {:msg "POST /discord/webhook", :line 80}

  {:header {:sig "e2697d400c2110893c92f36fea258df8435063f12bd98394a2514a9032c42c8767e20b75a7ebf7d6c8a11c01fbe2e3fc80fea6667d231afea00ef36c2310760f", :time "1625587780"},
   :public-key "db5b5a8d88a2918a03e52429cefc85cdd3a7f382d05b28cd411f40494dd26db6"}

  {:header {:x-forwarded-proto "https",
            :x-signature-timestamp "1625587780",
            :user-agent "Discord-Interactions/1.0 (+https://discord.com)",
            :via "1.1 vegur",
            :x-request-start "1625587780720",
            :x-forwarded-port "443",
            :x-signature-ed25519 "e2697d400c2110893c92f36fea258df8435063f12bd98394a2514a9032c42c8767e20b75a7ebf7d6c8a11c01fbe2e3fc80fea6667d231afea00ef36c2310760f",
            :host "super-dice-roll-discord-clj.herokuapp.com",
            :content-length "449", :content-type "application/json", :connect-time "1", :total-route-time "0", :connection "close", :x-forwarded-for "35.237.4.214",
            :x-request-id "1d8e8cef-94eb-4980-a3be-26e29518d50b",
            :accept "*/*"},
   :body {:id "862002369671594025", :application_id "861964097700757534", :type 1, :token "aW50ZXJhY3Rpb246ODYyMDAyMzY5NjcxNTk0MDI1OnZYMmFRTDhsTGQyMHZ6cGtHWk1RZ1Jvak1ocVljSXYyS3J3Sm1rbkd0ejFlaWhSelBBV0UwTHZaN1ZKaE11V2Z2N3VpNWNCZmJrdFFFOWFENGFnbmdiRDdTVmswQTcyOFVqTml1cFRxTHJMMVhsZUFvTEljckJIbmJrd0E2bkNk", :version 1, :user {:username "delboni", :id "598978693322375169", :avatar "42fe6008311f0f977ee9e9166cf261ff", :public_flags 0, :discriminator "4175"}}}

  (require 'cheshire.core)

  (def signature-bytes (hex->bytes-2 "e2697d400c2110893c92f36fea258df8435063f12bd98394a2514a9032c42c8767e20b75a7ebf7d6c8a11c01fbe2e3fc80fea6667d231afea00ef36c2310760f"))
  (def public-key-bytes (hex->bytes-2 "db5b5a8d88a2918a03e52429cefc85cdd3a7f382d05b28cd411f40494dd26db6"))

  (def timestamp-bytes (encode "1625587780" "utf8"))
  (def body-bytes (encode (cheshire.core/encode {:id "862002369671594025", :application_id "861964097700757534", :type 1, :token "aW50ZXJhY3Rpb246ODYyMDAyMzY5NjcxNTk0MDI1OnZYMmFRTDhsTGQyMHZ6cGtHWk1RZ1Jvak1ocVljSXYyS3J3Sm1rbkd0ejFlaWhSelBBV0UwTHZaN1ZKaE11V2Z2N3VpNWNCZmJrdFFFOWFENGFnbmdiRDdTVmswQTcyOFVqTml1cFRxTHJMMVhsZUFvTEljckJIbmJrd0E2bkNk", :version 1, :user {:username "delboni", :id "598978693322375169", :avatar "42fe6008311f0f977ee9e9166cf261ff", :public_flags 0, :discriminator "4175"}}) "utf8"))
  (def message-bytes (byte-array (+ (alength timestamp-bytes) (alength body-bytes))))
  (System/arraycopy timestamp-bytes 0 message-bytes 0 (alength timestamp-bytes))
  (System/arraycopy body-bytes 0 message-bytes (alength timestamp-bytes) (alength body-bytes))
  (vec message-bytes)

  (verify (new-verifier (Ed25519PublicKeyParameters. (hex->bytes "db5b5a8d88a2918a03e52429cefc85cdd3a7f382d05b28cd411f40494dd26db6") 0))
          message-bytes
          (hex->bytes "e2697d400c2110893c92f36fea258df8435063f12bd98394a2514a9032c42c8767e20b75a7ebf7d6c8a11c01fbe2e3fc80fea6667d231afea00ef36c2310760f"))

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

  (verify v msg signature)
  (verify v (digest-stream msg) signature)
  ;; true
  )

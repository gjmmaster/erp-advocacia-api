(ns juridico.api.storage.r2
  "Cloudflare R2 storage integration for document uploads.
   R2 is S3-compatible, so we use the AWS SDK."
  (:require [amazonica.aws.s3 :as s3]
            [amazonica.core :as aws]
            [clojure.java.io :as io]
            [clojure.tools.logging :as log]))

;; R2 Configuration from environment variables
(def r2-config
  {:access-key (System/getenv "R2_ACCESS_KEY_ID")
   :secret-key (System/getenv "R2_SECRET_ACCESS_KEY")
   :endpoint (System/getenv "R2_ENDPOINT")})

(def bucket (System/getenv "R2_BUCKET"))

(defn- validate-config
  "Validates that all required R2 configuration is present."
  []
  (when-not (:access-key r2-config)
    (throw (ex-info "R2_ACCESS_KEY_ID not configured" {})))
  (when-not (:secret-key r2-config)
    (throw (ex-info "R2_SECRET_ACCESS_KEY not configured" {})))
  (when-not (:endpoint r2-config)
    (throw (ex-info "R2_ENDPOINT not configured" {})))
  (when-not bucket
    (throw (ex-info "R2_BUCKET not configured" {}))))

(defn upload-file!
  "Uploads a file to Cloudflare R2.
   
   Parameters:
   - tenant-id: ID of the tenant (for isolation)
   - processo-id: ID of the processo
   - file-name: Original filename
   - file-bytes: File content as byte array
   - content-type: MIME type of the file
   
   Returns: The R2 key (path) of the uploaded file"
  [tenant-id processo-id file-name file-bytes content-type]
  (validate-config)
  
  (let [timestamp (System/currentTimeMillis)
        ;; Create a unique key with tenant isolation
        key (str "tenants/" tenant-id 
                 "/processos/" processo-id 
                 "/" timestamp "_" file-name)]
    
    (log/info "Uploading file to R2" 
              {:tenant-id tenant-id 
               :processo-id processo-id 
               :file-name file-name
               :size (count file-bytes)
               :key key})
    
    (try
      ;; Upload to R2 using S3-compatible API
      (aws/with-credential r2-config
        (s3/put-object
          :bucket-name bucket
          :key key
          :input-stream (io/input-stream file-bytes)
          :metadata {:content-type content-type
                     :content-length (count file-bytes)
                     :tenant-id (str tenant-id)
                     :processo-id (str processo-id)}))
      
      (log/info "File uploaded successfully" {:key key})
      key
      
      (catch Exception e
        (log/error e "Failed to upload file to R2")
        (throw (ex-info "Upload failed" 
                       {:cause (.getMessage e)} 
                       e))))))

(defn get-presigned-url
  "Generates a temporary presigned URL for downloading a file.
   URL is valid for 1 hour.
   
   Parameters:
   - key: The R2 key (path) of the file
   
   Returns: A presigned URL string"
  [key]
  (validate-config)
  
  (log/info "Generating presigned URL" {:key key})
  
  (try
    (aws/with-credential r2-config
      (s3/generate-presigned-url
        :bucket-name bucket
        :key key
        :method "GET"
        :expiration (+ (System/currentTimeMillis) (* 60 60 1000))))  ; 1 hour
    
    (catch Exception e
      (log/error e "Failed to generate presigned URL")
      (throw (ex-info "Failed to generate download URL" 
                     {:cause (.getMessage e)} 
                     e)))))

(defn delete-file!
  "Deletes a file from R2.
   
   Parameters:
   - key: The R2 key (path) of the file to delete
   
   Returns: true if successful"
  [key]
  (validate-config)
  
  (log/info "Deleting file from R2" {:key key})
  
  (try
    (aws/with-credential r2-config
      (s3/delete-object
        :bucket-name bucket
        :key key))
    
    (log/info "File deleted successfully" {:key key})
    true
    
    (catch Exception e
      (log/warn e "Failed to delete file from R2" {:key key})
      ;; Don't throw - soft delete in DB is more important
      false)))

(defn list-files
  "Lists all files for a specific processo.
   
   Parameters:
   - tenant-id: ID of the tenant
   - processo-id: ID of the processo
   
   Returns: List of file objects"
  [tenant-id processo-id]
  (validate-config)
  
  (let [prefix (str "tenants/" tenant-id "/processos/" processo-id "/")]
    
    (log/info "Listing files from R2" {:prefix prefix})
    
    (try
      (aws/with-credential r2-config
        (let [result (s3/list-objects
                       :bucket-name bucket
                       :prefix prefix)]
          (:object-summaries result)))
      
      (catch Exception e
        (log/error e "Failed to list files from R2")
        []))))

(defn health-check
  "Checks if R2 is properly configured and accessible.
   
   Returns: {:healthy true/false :message string}"
  []
  (try
    (validate-config)
    (aws/with-credential r2-config
      (s3/list-buckets))
    {:healthy true :message "R2 connection OK"}
    
    (catch Exception e
      {:healthy false :message (.getMessage e)})))

import { useState } from "react";
import axios from "axios";

const FileUpload = () => {
  const [file, setFile] = useState(null);
  const [uploadUrl, setUploadUrl] = useState("");

  const handleFileChange = (event) => {
    setFile(event.target.files[0]);
  };

  const handleUpload = async () => {
    if (!file) {
      alert("Please select a file first.");
      return;
    }

    const formData = new FormData();
    formData.append("file", file);

    try {
      const response = await axios.post(
        "http://localhost:8080/api/files/upload", // Direct Spring Boot API URL
        formData,
        {
          headers: {
            "Content-Type": "multipart/form-data",
          },
        }
      );

      setUploadUrl(response.data); // Public URL returned from backend
      alert("File uploaded successfully!");
    } catch (error) {
      console.error("Upload failed:", error);
      alert("Failed to upload file.");
    }
  };

  return (
    <div>
      <input type="file" onChange={handleFileChange} />
      <button onClick={handleUpload}>Upload</button>
      {uploadUrl && (
        <div>
          <p>File uploaded successfully:</p>
          <a href={uploadUrl} target="_blank" rel="noopener noreferrer">
            {uploadUrl}
          </a>
        </div>
      )}
    </div>
  );
};

export default FileUpload;

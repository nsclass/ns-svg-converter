import React, { useState, useCallback } from "react"
import { useDropzone } from "react-dropzone"

import "../assets/css/dropzone.css"

const ImageDropZone = () => {
    const [filename, updateFilename] = useState("")
    const [fileContent, updateFileContent] = useState()
    const [errorMessage, updateErrorMessage] = useState()

    const loadFile = file => {
        try {
            updateFilename(file.name)

            let reader = new FileReader()
            reader.onloadend = evt => {
                if (evt.target.readyState == FileReader.DONE) {
                    const content = String(evt.target.result)
                    updateFileContent(content)
                }
            }
            reader.readAsText(file)
            updateErrorMessage(null)
        } catch (e) {
            console.log(e)
            updateErrorMessage(JSON.stringify(e, null, 2))
        }
    }

    const onDrop = useCallback(acceptedFiles => {
        loadFile(acceptedFiles[0])
    }, [])
    const { getRootProps, getInputProps, isDragActive } = useDropzone({ onDrop })

    return (
        <div className="dropzone-container">
            <div className="dropzone" {...getRootProps()}>
                <input {...getInputProps()} />
                {isDragActive ? (
                    <p>Drop the files here ...</p>
                ) : (
                    <p>Drag 'n' drop some files here, or click to select files</p>
                )}
            </div>
        </div>
    )
}

export default ImageDropZone

import React from "react"
import useImageDropZone from "./ImageDropZone";

const MainView = () => {
    const [filename, fileContent, ImageDropZone] = useImageDropZone()

    return (
        <header className="masthead">
            <div className="container">
                <div className="intro-text">
                    <div>
                        <h1>SVG Image Converter</h1>
                    </div>
                    <div>
                        <h5>(Supported image types are PNG and JFG)</h5>
                        <h5>(Max supported image size is 2MB due to available memory in Heroku)</h5>
                    </div>
                    <ImageDropZone/>
                </div>
            </div>
        </header>
    )
}

export default MainView

import React from "react"
import useImageDropZone from "./ImageDropZone"
import SVGHistory from "./SVGHistory"

const MainView = () => {
    const [filename, fileContent, ImageDropZone] = useImageDropZone()

    const SVGConverterView = () => {
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

    return (
        <div>
            <SVGConverterView />
            <SVGHistory />
        </div>
    )
}

export default MainView

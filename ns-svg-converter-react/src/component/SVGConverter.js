import React, { useState } from "react"

const SVGConvertingView = ({ imageFilename, loading, svgData }) => {
    if (loading) {
        return (
            <div>
                <div className="portfolio-caption text-center">
                    <h4>Converting an image({imageFilename})...</h4>
                    <i className="fa fa-refresh fa-spin fa-3x fa-fw"></i>
                    <span className="sr-only">Loading...</span>
                </div>
            </div>
        )
    }

    if (!svgData) {
        return null
    }

    return (
        <div>
            {/* <div className="svg" [innerHTML]="svg"></div> */}
            <div className="portfolio-caption">
                <h4>Converted SVG image</h4>
                <button className="btn btn-success">Download</button>
            </div>
        </div>
    )
}

const SVGConverter = ({ imageFilename, imageData }) => {
    const [loading, updateLoading] = useState(false)
    const [svgData, updateSVGData] = useState()

    if (!imageData) {
        return null
    }

    return (
        <div className="row col-lg-12 text-center">
            <div className="col-md-6 col-sm-6 portfolio-item">
                <img className="img-fluid" src={imageData} />
                <div className="portfolio-caption">
                    <h4>Original image</h4>
                    <button className="btn">Convert</button>
                </div>
            </div>

            <div className="col-md-6 col-sm-6 align-self-center">
                <SVGConvertingView
                    imageFilename={imageFilename}
                    loading={loading}
                    svgData={svgData}
                />
            </div>
        </div>
    )
}

export default SVGConverter

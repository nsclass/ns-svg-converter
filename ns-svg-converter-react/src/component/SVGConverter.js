import React, { useState } from "react"
import axios from "axios"

const SVGConvertingView = ({ imageFilename, loading, svgData, errorMsg }) => {
  const downloadSvgFile = () => {
    const element = document.createElement("a")
    const file = new Blob([svgData], { type: "text/plain" })
    element.href = URL.createObjectURL(file)
    element.download = `${imageFilename}.svg`
    document.body.appendChild(element) // Required for this to work in FireFox
    element.click()
  }

  if (loading) {
    return (
      <div className="mx-auto">
        <div className="portfolio-caption text-center">
          <h4>Converting an image({imageFilename})...</h4>
          <i className="fa fa-refresh fa-spin fa-3x fa-fw"></i>
          <span className="sr-only">Loading...</span>
        </div>
      </div>
    )
  }

  if (errorMsg) {
    return (
      <div className="alert alert-danger">
        <strong>Error!</strong> {errorMsg}
      </div>
    )
  }

  if (!svgData) {
    return null
  }

  const convertSvgToBase64ImgString = (SVG) =>
    `data:image/svg+xml;base64,${Buffer.from(SVG).toString("base64")}`

  return (
    <div className="mx-auto">
      <img src={convertSvgToBase64ImgString(svgData)} />
      <div className="portfolio-caption">
        <h4>Converted SVG image</h4>
        <button className="btn btn-success" onClick={(e) => downloadSvgFile()}>
          Download
        </button>
      </div>
    </div>
  )
}

const SVGConverter = ({ imageFilename, imageData }) => {
  const [loading, updateLoading] = useState(false)
  const [svgData, updateSVGData] = useState()
  const [errorMsg, updateErrorMsg] = useState()

  const convertSvg = async () => {
    try {
      const requestData = {
        imageFilename: imageFilename,
        imageDataBase64: imageData,
        numberOfColors: 16,
      }

      updateSVGData(null)
      updateLoading(true)
      updateErrorMsg(null)

      const { data } = await axios.put("/api/v1/svg/conversion", requestData)
      updateSVGData(data.svgString)
    } catch (error) {
      console.error(error)

      let message = error.message
      if (error.response && error.response.data && error.response.data.message) {
        message = error.response.data.message
      }
      const splitRes = message.split(":")
      if (splitRes.length > 1) {
        updateErrorMsg(splitRes[1])
      } else {
        updateErrorMsg(message)
      }
    }

    updateLoading(false)
  }

  if (!imageData) {
    return null
  }

  return (
      <>
        <div className="row col-lg-12 text-center">
          <div className="mx-auto">
            <img className="img-fluid" src={imageData} />
            <div className="portfolio-caption">
              <h4>Original image</h4>
              <button className="btn btn-success" onClick={(e) => convertSvg()}>
                Convert
              </button>
            </div>
          </div>
        </div>

        <div className="row col-lg-12 text-center">
          <SVGConvertingView
              imageFilename={imageFilename}
              loading={loading}
              svgData={svgData}
              errorMsg={errorMsg}
          />
        </div>
      </>
  )
}

export default SVGConverter

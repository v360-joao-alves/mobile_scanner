#ifndef FLUTTER_PLUGIN_MOBILE_SCANNER_PLUGIN_H_
#define FLUTTER_PLUGIN_MOBILE_SCANNER_PLUGIN_H_

#include <flutter/method_channel.h>
#include <flutter/plugin_registrar_windows.h>
#include <flutter/texture_registrar.h>
#include <flutter/standard_method_codec.h>

#include <opencv2/opencv.hpp>
#include <zxing/DecodeHints.h>
#include <zxing/MultiFormatReader.h>
#include <zxing/common/HybridBinarizer.h>
#include <zxing/common/GlobalHistogramBinarizer.h>
#include <zxing/Binarizer.h>
#include <zxing/MatSource.h>

#include <memory>
#include <vector>
#include <thread>
#include <atomic>
#include <condition_variable>

namespace mobile_scanner {

    class MobileScannerPlugin : public flutter::Plugin {
    public:
        static void RegisterWithRegistrar(flutter::PluginRegistrarWindows* registrar);

        MobileScannerPlugin(flutter::TextureRegistrar* texture_registrar);

        virtual ~MobileScannerPlugin();

        // Handle method calls from Flutter
        void HandleMethodCall(
            const flutter::MethodCall<flutter::EncodableValue>& method_call,
            std::unique_ptr<flutter::MethodResult<flutter::EncodableValue>> result);

    private:
        void StartCamera();
        void StopCamera();
        void CaptureFrame();
        void ProcessFrame(cv::Mat frame);

        flutter::TextureRegistrar* texture_registrar_;
        std::atomic<bool> running_;
        std::thread camera_thread_;
        std::condition_variable cv_;
        std::mutex mutex_;

        int64_t texture_id_;
        std::shared_ptr<flutter::TextureVariant> texture_;

        std::unique_ptr<zxing::MultiFormatReader> barcode_reader_;
    };

}  // namespace mobile_scanner

#endif  // FLUTTER_PLUGIN_MOBILE_SCANNER_PLUGIN_H_

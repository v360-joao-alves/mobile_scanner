import 'package:flutter/material.dart';
import 'package:mobile_scanner/mobile_scanner.dart';

/// Widget to display scanned barcodes.
class ScannedBarcodeLabel extends StatelessWidget {
  /// Construct a new [ScannedBarcodeLabel] instance.
  const ScannedBarcodeLabel({required this.barcodes, super.key});

  /// Barcode stream for scanned barcodes to display
  final Stream<BarcodeCapture> barcodes;

  @override
  Widget build(BuildContext context) {
    return StreamBuilder(
      stream: barcodes,
      builder: (context, snapshot) {
        final List<Barcode> scannedBarcodes = snapshot.data?.barcodes ?? [];

        if (scannedBarcodes.isEmpty) {
          return const Text(
            'Scan something!',
            overflow: TextOverflow.fade,
            style: TextStyle(color: Colors.white),
          );
        }

        final String displayValues = scannedBarcodes
            .map((e) => e.displayValue)
            .join('\n');

        debugPrint('debug displayValue: ${scannedBarcodes.first.displayValue}');
        debugPrint('debug rawValue: ${scannedBarcodes.first.rawValue}');
        debugPrint('debug rawBytes: ${scannedBarcodes.first.rawBytes}');
        debugPrint('debug rawPayloadData: '
            '${scannedBarcodes.first.rawPayloadData}');

        if (scannedBarcodes.any((e) => e.displayValue != null)) {
          return Text(
            'Display values: $displayValues',
            overflow: TextOverflow.fade,
            style: const TextStyle(color: Colors.white),
          );
        }

        final String rawValues = scannedBarcodes
            .map((e) => e.rawValue)
            .join('\n');

        if (scannedBarcodes.any((e) => e.rawValue != null)) {
          return Text(
            'Raw values: $rawValues',
            overflow: TextOverflow.fade,
            style: const TextStyle(color: Colors.white),
          );
        }

        final String rawBytes = scannedBarcodes
            .map((e) => e.rawBytes)
            .join('\n');

        return Text(
          'Raw bytes: $rawBytes',
          overflow: TextOverflow.fade,
          style: const TextStyle(color: Colors.white),
        );
      },
    );
  }
}

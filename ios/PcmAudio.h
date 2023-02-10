
#ifdef RCT_NEW_ARCH_ENABLED
#import "RNPcmAudioSpec.h"

@interface PcmAudio : NSObject <NativePcmAudioSpec>
#else
#import <React/RCTBridgeModule.h>

@interface PcmAudio : NSObject <RCTBridgeModule>
#endif

@end

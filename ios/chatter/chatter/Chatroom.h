//
//  Chatroom.h
//  chatter
//
//  Created by sean matthews on 3/7/14.
//  Copyright (c) 2014 rowboat entertainment. All rights reserved.
//

@import Foundation;
@import CoreLocation;

@interface Chatroom : NSObject
{
    long long chatId;
}

@property (atomic, strong) NSMutableArray* chatQueue;
@property (nonatomic) long long chatroomId;
@property (nonatomic, strong) NSMutableDictionary* members;
@property (nonatomic) CLLocationCoordinate2D origin;
@property (nonatomic) long long radius;

@end

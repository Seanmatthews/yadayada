//
//  UITableViewCell+UITableViewCellCategory.m
//  chatter
//
//  Created by sean matthews on 10/29/13.
//  Copyright (c) 2013 rowboat entertainment. All rights reserved.
//

#import "UITableViewCell+UITableViewCellCategory.h"
#import <objc/runtime.h>
#import <objc/message.h>

@implementation UITableViewCell (UITableViewCellCategory)

+ (void)load
{
    Method existing = class_getInstanceMethod(self, @selector(layoutSubviews));
    Method new = class_getInstanceMethod(self, @selector(_autolayout_replacementLayoutSubviews));
    
    method_exchangeImplementations(existing, new);
}

- (void)_autolayout_replacementLayoutSubviews
{
    [super layoutSubviews];
    [self _autolayout_replacementLayoutSubviews]; // not recursive due to method swizzling
    [super layoutSubviews];
}

@end

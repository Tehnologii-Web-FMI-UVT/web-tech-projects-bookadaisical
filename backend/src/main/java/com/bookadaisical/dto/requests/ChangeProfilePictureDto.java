package com.bookadaisical.dto.requests;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@NoArgsConstructor
public class ChangeProfilePictureDto {
    String username;
    String image;
}

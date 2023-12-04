package com.bookadaisical.dto.responses;

import com.bookadaisical.dto.responses.interfaces.IUserDto;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class UserSlimDto implements IUserDto {
    private int id;
    private String username;
}
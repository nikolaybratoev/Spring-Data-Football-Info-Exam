package softuni.exam.domain.dtos;

import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import java.util.List;

import static javax.xml.bind.annotation.XmlAccessType.FIELD;

@XmlRootElement(name = "pictures")
@XmlAccessorType(FIELD)
public class PictureSeedRootDto {

    @XmlElement(name = "picture")
    List<PictureSeedDto> pictures;

    public PictureSeedRootDto() {
    }

    public List<PictureSeedDto> getPictures() {
        return pictures;
    }

    public void setPictures(List<PictureSeedDto> pictures) {
        this.pictures = pictures;
    }
}
